package com.elephant.proxy.handler;

import com.elephant.NettyBootstrapInitializer;
import com.elephant.YrpcBootstrap;
import com.elephant.discovery.Registry;
import com.elephant.enumeration.RequestType;
import com.elephant.exception.DiscoveryException;
import com.elephant.exception.NetworkException;
import com.elephant.transport.message.RequestPayload;
import com.elephant.transport.message.YrpcRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/20/14:52
 * @Description: 该类封装了服务调用端通讯的基础逻辑，每一个代理对象的远程调用过程都封装在了 invoke 方法中
 * @function：1.发现可用服务 2.建立连接 3.发送请求 4.得到结果
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    private Class<?> interfaceRef;

    private Registry registry;
    // 分组信息
    private String group;

    public RpcConsumerInvocationHandler(Class<?> interfaceRef, Registry registry, String group) {
        this.interfaceRef = interfaceRef;
        this.registry = registry;
        this.group = group;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //从注册中心中寻找一个可用服务
        //TODO 后期可以修改为自动推荐一个服务 -- 负载均衡
        //TODO 每次调用都去注册中心拉取服务 效率低下 -- 本地缓存 + watcher 机制
        List<InetSocketAddress> addresses = registry.lookup(interfaceRef.getName(),group);
        InetSocketAddress address  = addresses.get(0);
        if(log.isDebugEnabled()){
            log.debug("选择了该服务：【{}】下的【{}】节点",interfaceRef.getName(),address);
        }

        //2.使用 Netty 发起 RPC 调用
        //应该将 Netty 的连接进行缓存，每一次建立一个新的连接是不合理的

        Channel channel = getAvailableChannel(address);

        /**
         * ------------------封装报文-------------------------
         */
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();

        //TODO 修改临时参数
        YrpcRequest yrpcRequest = YrpcRequest.builder()
                .requestId(1L)
                .compressType((byte) 1)
                .requestType(RequestType.REQUEST.getId())
                .serializeType((byte) 1)
                .requestPayload(requestPayload)
                .build();


        //发送请求 每一个 RPC 服务维护着一个 CompletableFuture
        /**
         * ------------------同步策略-------------------------
         */

//                ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
        // 需要学习channelFuture的简单的api get 阻塞获取结果，getNow 获取当前的结果，如果未处理完成，返回null
//                if(channelFuture.isDone()){
//                    Object object = channelFuture.getNow();
//                } else if( !channelFuture.isSuccess() ){
//                    // 需要捕获异常,可以捕获异步任务中的异常
//                    Throwable cause = channelFuture.cause();
//                    throw new RuntimeException(cause);
//                }

        /**
         * ------------------异步策略-------------------------
         */
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        YrpcBootstrap.PENDING_REQUEST.put(1L,completableFuture);
        //写出请求,这个请求的实例会进入 pipeline
        channel.writeAndFlush(yrpcRequest).addListener((ChannelFutureListener) promise -> {
            // 当前的promise将来返回的结果是什么，writeAndFlush的返回结果
            // 一旦数据被写出去，这个promise也就结束了
            // 但是我们想要的是什么？  服务端给我们的返回值，所以这里处理completableFuture是有问题的
            // 是不是应该将 completableFuture 挂起并且暴露，并且在得到服务提供方的响应的时候调用complete方法
//                    if(promise.isDone()){
//                        completableFuture.complete(promise.getNow());
//                    }

            // 只需要处理以下异常就行了
            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });

        // 如果没有地方处理这个 completableFuture，这里会阻塞，等待 complete 方法的执行
        // 我们需要在那里调用 complete 方法得到结果，就是 pipeline 中最终的 handler 的处理结果
        /**
         * @see NettyBootstrapInitializer
         */
        return completableFuture.get(10, TimeUnit.SECONDS);
    }


    /**
     * 获取可用的 channel
     * @param address 地址
     * @return channel
     */
    private Channel getAvailableChannel(InetSocketAddress address) {

        //先从缓存中取
        Channel channel = YrpcBootstrap.CHANNEL_CACHE.get(address);

        /**
         * 同步建立 channel 连接
         */

//                if(channel == null){
//                    //建立连接
//                    NioEventLoopGroup group = new NioEventLoopGroup();
//                    try {
//                        Bootstrap bootstrap = new Bootstrap();
//                        //引导程序
//                        bootstrap = bootstrap.group(group)
//                                .channel(NioSocketChannel.class)
//                                .remoteAddress(address)
//                                .handler(new ChannelInitializer<SocketChannel>() {
//                                    @Override
//                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
//                                        //TODO 服务调用端处理数据
//                                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
//                                            @Override
//                                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
//                                                //TODO
//                                                channelHandlerContext.channel().writeAndFlush("");
//                                            }
//                                        });
//                                    }
//                                });
//                        //尝试连接
//                        channel = bootstrap.connect(address).sync().channel();
//                        YrpcBootstrap.CHANNEL_CACHE.put(address,channel);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }

        /**
         * 异步建立 channel 连接
         */
        if(channel == null){
            // await 方法会阻塞，会等待连接成功再返回【需要考虑超时问题】，Netty 也提供了异步处理的逻辑
            // sync 和 await都是阻塞当前线程，获取返回值【连接的过程时异步的，发生数据的过程时异步的】
            // 如果发生了异常，sync会主动在主线程中抛出异常，异常在子线程中处理需要使用 future 中处理
            // 同步操作
            // channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
            // 异步操作
            // CompletableFuture 拿到服务器响应的结果
            CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise ->{
                if(promise.isDone()){
                    completableFuture.complete(promise.channel());
                } else if (!promise.isSuccess()) {
                    //捕捉异常
                    completableFuture.completeExceptionally(promise.cause());
                }
            });
            //阻塞拿到 channel
            try {
                channel = completableFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取 channel 时发生异常",e);
                throw new DiscoveryException(e);
            }

            YrpcBootstrap.CHANNEL_CACHE.put(address,channel);
        }

        if(channel == null){
            throw new NetworkException("获取通道异常");
        }
        return channel;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<?> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<?> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }
}
