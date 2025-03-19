package com.elephant;

import com.elephant.discovery.Registry;
import com.elephant.discovery.RegistryConfig;
import com.elephant.exception.DiscoveryException;
import com.elephant.exception.NetworkException;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/16:19
 * @Description: 服务调用端核心配置类
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private Registry registry;
    // 分组信息
    private String group;



    /**
     * 代理设计模式 生成一个 API 的代理对象
     * @return 代理对象
     */
    public T get(){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        //动态代理
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //从注册中心中寻找一个可用服务
                //TODO 后期可以修改为自动推荐一个服务 -- 负载均衡
                //TODO 每次调用都去注册中心拉取服务 效率低下 -- 本地缓存 + watcher 机制
                List<InetSocketAddress> addresses = registry.lookup(interfaceRef.getName(),group);
                InetSocketAddress address  = addresses.get(0);
                log.info("选择了该服务：【{}】下的【{}】节点",interfaceRef.getName(),address);


                //2.使用 Netty 发起 RPC 调用
                //应该将 Netty 的连接进行缓存，每一次建立一个新的连接是不合理的
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
                    //await 方法会阻塞，会等待连接成功再返回【需要考虑超时问题】，Netty 也提供了异步处理的逻辑
                    //sync 和 await都是阻塞当前线程，获取返回值【连接的过程时异步的，发生数据的过程时异步的】
                    //如果发生了异常，sync会主动在主线程中抛出异常，异常在子线程中处理需要使用 future 中处理
                    //同步操作
                    //channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
                    //异步操作
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
                    channel = completableFuture.get(3, TimeUnit.SECONDS);

                    YrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }

                if(channel == null){
                    throw new NetworkException("获取通道异常");
                }
                //发送请求
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
                //TODO 写出客户调用端接受
                channel.writeAndFlush(Unpooled.copiedBuffer("hello".getBytes())).addListener((ChannelFutureListener) promise -> {
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

                //TODO 超时时间可以动态调整
//                return completableFuture.get(3, TimeUnit.SECONDS);
                return null;
            }
        });
        return (T)helloProxy;

    }


    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
