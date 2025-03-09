package com.elephant.proxy.handler;

import com.elephant.ElephantRPCBootstrap;
import com.elephant.discovery.NettyBootstrapInitializer;
import com.elephant.discovery.Registry;
import com.elephant.exception.DiscoveryException;
import com.elephant.transport.message.ElephantRPCRequest;
import com.elephant.transport.message.RequestPayload;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/09/10:06
 * @Description: 该类封装了客户端通讯的基础逻辑，每一个代理对象的远程调用过程都封装在了 invoke 方法中
 *                 1.发现可用服务
 *                 2.建立连接
 *                 3.发送请求
 *                 4.得到结果
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    //需要一个注册中心
    private final Registry registry;

    //需要一个接口引用
    private final Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //1.发现服务，从注册中心，寻找一个可用的服务
        //传入服务名称，返回连接 URL【IP + Port】
        InetSocketAddress address = registry.searchService(interfaceRef.getName());
        if(log.isDebugEnabled()){
            log.debug("**** The service consumer has discovered the available host of the service【{}】.",
                    interfaceRef.getName(),address);
        }
        //2.使用 netty 连接服务，发送调用的服务的名字 + 方法名字 + 参数列表，得到结果
        //q: 如果整个连接过程中放在这里，意味着每次建立连接都会产生一个新的 Netty连接。不合适
        //解决方案：
        //  1使用 Map 集合缓存
        //  2.进行封装

        Channel channel = getAvailableChannel(address);
        if (log.isDebugEnabled()){
            log.debug("**** Successfully obtained the channel.");
        }

        /**
         * -------------------------------封装报文--------------------------------
         */
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnTyp(method.getReturnType())
                .build();

        ElephantRPCRequest elephantRPCRequest = ElephantRPCRequest.builder()
                //TODO 需要自动生成一个全局 ID
                .requestId(1L)
                .compressType((byte) 1)
                .requestType((byte) 1)
                .serializeType((byte) 1)
                .requestPayload(requestPayload)
                .build();

        /**
     * ------------------------------------同步策略---------------------------------
     */
//                ChannelFuture channelFuture = channel.writeAndFlush(new Object());
//                //get 阻塞获取结果 getNow 获取当前结果，如果未获取完成，返回 null
//                if(channelFuture.isDone()){
//                    Object object = channelFuture.getNow();
//                }else if(!channelFuture.isSuccess()){
//                    //捕获异常,子线程可以捕获异常
//                    Throwable cause = channelFuture.cause();
//                    throw new RuntimeException(cause);
//                }

        /**
         * ------------------------------------异步策略---------------------------------
         */
            CompletableFuture<Object> completableFuture = new CompletableFuture<>();

            //completableFuture 挂起并暴露
            ElephantRPCBootstrap.PENDING_REQUEST.put(1L,completableFuture);

            //写出一个请求，这个请求的实例就会进入 pipeline 执行出战的操作
            //出战就是 elephantRPCRequest ---> 二进制报文
            channel.writeAndFlush(elephantRPCRequest).addListener((ChannelFutureListener) promise ->{
            //当前的 promise 将来返回的结果是什么 --- writeAndFlush的返回结果
            //一旦数据被写出去，这个 promise 也就结束了
            //但是想要的是服务端给的返回值，completableFuture.complete(promise.getNow());不能这样写。
            //应该将 completableFuture 挂起并暴露，并且在得到服务提供方响应时调用 complete 方法

            //所以只需要处理异常就行了
            if (!promise.isSuccess()){
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        //如果没有地方处理这个 completableFuture 这里会阻塞，等待 complete 方法的执行
        //q：需要在哪里调用这个 complete 方法得到结果 --- pipeline 最终的处理器中
        //这里会异步等待，等待 Netty 通过异步的方式，在 handler 中处理 completableFuture 方法
            return completableFuture.get(10,TimeUnit.SECONDS);
    }

    /**
     * 根据地址获取一个可用通道
     * @param address
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        Channel channel = ElephantRPCBootstrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
            //sync 和 await 都是阻塞线程当前线程，获取返回值（连接的过程是异步，发送数据的过程是异步的）
            //如果发生了异常，sync 会在线程抛出异常。await不会，异常在子线程中处理需要使用 future 中处理。
            //channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
            //ElephantRPCBootstrap.CHANNEL_CACHE.put(address,channel);
            //使用 addListener 执行的异步操作
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();

            //启动 Netty
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener(
                    (ChannelFutureListener) promise -> {
                        if (promise.isDone()) {
                            //在主线程中拿到子线程的参数
                            if (log.isDebugEnabled()) {
                                log.debug("Haven built the connection successfully with:{}", address);
                            }
                            channelFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    });
            //阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("**** An exception occurred while acquired a channel.");
                throw new DiscoveryException(e);
            }
            //还是为空
            if (channel == null){
                log.error("**** An exception occurred while obtaining or establishing a {} channel.",address);
            }
            //缓存
            ElephantRPCBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        return channel;
    }
}
