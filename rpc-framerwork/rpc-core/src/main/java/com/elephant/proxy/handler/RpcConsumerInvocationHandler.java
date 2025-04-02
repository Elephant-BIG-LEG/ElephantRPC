package com.elephant.proxy.handler;

import com.elephant.NettyBootstrapInitializer;
import com.elephant.YrpcBootstrap;
import com.elephant.annotation.TryTimes;
import com.elephant.compress.CompressorFactory;
import com.elephant.discovery.Registry;
import com.elephant.enumeration.RequestType;
import com.elephant.exception.DiscoveryException;
import com.elephant.exception.NetworkException;
import com.elephant.serialize.SerializerFactory;
import com.elephant.transport.message.RequestPayload;
import com.elephant.transport.message.YrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
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

    /**
     * 本质上，所有的方法调用都会走到这里
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        int tryTime = 0;
        int intervalTime = 0;

        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);
        if(tryTimesAnnotation != null){
            tryTime = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.intervalTime();
        }


        /**
         * ------------------ 封装报文 -------------------------
         */
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();

        if(requestPayload == null){
            throw new RuntimeException("为null");
        }

        /**
         * ------------------ 生成请求 -------------------------
         */
        YrpcRequest yrpcRequest = YrpcRequest.builder()
                .requestId(YrpcBootstrap.getInstance().configuration.idGenerator.getId())
                .compressType(CompressorFactory.getCompressor(YrpcBootstrap.getInstance().configuration.getCompressType()).getCode())
                .requestType(RequestType.REQUEST.getId())
                .timeStamp(new Date().getTime())
                .serializeType(SerializerFactory.getSerializer(YrpcBootstrap.getInstance().configuration.getSerializeType()).getCode())
                .requestPayload(requestPayload)
                .build();


        while (true) {

            try {
                // 将请求存入本地线程 -- 为了负载均衡器可以获取到请求信息
                YrpcBootstrap.REQUEST_THREAD_LOCAL.set(yrpcRequest);

                // 从注册中心拉取服务列表，再从客户端负载均衡选择一个可用服务
                // TODO 每次调用都去注册中心拉取服务 效率低下 -- 本地缓存 + watcher 机制
                // 负载均衡
                InetSocketAddress address = YrpcBootstrap.getInstance().configuration.getLoadBalancer()
                        .selectServiceAddress(interfaceRef.getName(), group);

                if (log.isDebugEnabled()) {
                    log.debug("选择了该服务：【{}】下的【{}】节点", interfaceRef.getName(), address);
                }

                //2.使用 Netty 发起 RPC 调用
                //应该将 Netty 的连接进行缓存，每一次建立一个新的连接是不合理的

                Channel channel = getAvailableChannel(address);

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
                YrpcBootstrap.PENDING_REQUEST.put(yrpcRequest.getRequestId(), completableFuture);
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

                //清理 threadLocal
                YrpcBootstrap.REQUEST_THREAD_LOCAL.remove();

                /**
                 * @see NettyBootstrapInitializer
                 * 5s 后超时 抛出异常
                 * 如果没有地方处理这个 completableFuture，这里会阻塞，等待 complete 方法的执行
                 * 我们需要在那里调用 complete 方法得到结果，就是 pipeline 中最终的 handler 的处理结果
                 * TODO 感觉这里的时间限制应该可以动态修改
                 */
                return completableFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                tryTime--;
                try {
                    Thread.sleep(intervalTime);
                    log.error("调用远程接口失败，正在请求重试~~~~");
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                if(tryTime < 0){
                    log.debug("重试机会已经用完，请重新进行远程调用");
                    break;
                }
                log.error("正在进行第：【{}】次调用",3 - tryTime);
            }
        }
        throw new RuntimeException("执行远程方法调用接口失败");
    }


    /**
     * 获取可用的 channel
     *
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
        if (channel == null) {
            // await 方法会阻塞，会等待连接成功再返回【需要考虑超时问题】，Netty 也提供了异步处理的逻辑
            // sync 和 await都是阻塞当前线程，获取返回值【连接的过程时异步的，发生数据的过程时异步的】
            // 如果发生了异常，sync会主动在主线程中抛出异常，异常在子线程中处理需要使用 future 中处理
            // 同步操作
            // channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
            // 异步操作
            // CompletableFuture 拿到服务器响应的结果
            CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                if (promise.isDone()) {
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
                log.error("获取 channel 时发生异常", e);
                throw new DiscoveryException(e);
            }

            YrpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }

        if (channel == null) {
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
