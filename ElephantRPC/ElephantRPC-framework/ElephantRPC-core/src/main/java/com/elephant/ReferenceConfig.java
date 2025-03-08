package com.elephant;

import com.elephant.discovery.NettyBootstrapInitializer;
import com.elephant.discovery.Registry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/22:53
 * @Description: TODO
 */
@Slf4j
public class ReferenceConfig<T> {
    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    private Class<T> interfaceRef;

    private Registry registry;

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }


    /**
     * 代理设计模式，生成一个 API 接口的代理对象
     * @return
     */
    public T get() {
        //一定是使用动态代理 获取代理对象
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //当使用这个代理对象调用方法时，就会走到这个 invoke 方法
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
                Channel channel = ElephantRPCBootstrap.CHANNEL_CACHE.get(address);
                if(channel == null){
                    //sync 和 await 都是阻塞线程当前线程，获取返回值（连接的过程是异步，发送数据的过程是异步的）
                    //如果发生了异常，sync 会在线程抛出异常。await不会，异常在子线程中处理需要使用 future 中处理。
//                    channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
//                    ElephantRPCBootstrap.CHANNEL_CACHE.put(address,channel);
                    //使用 addListener 执行的异步操作
                    CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
                    NettyBootstrapInitializer.getBootstrap().connect(address).addListener(
                            (ChannelFutureListener)promise ->{
                                if(promise.isDone()){
                                    //在主线程中拿到子线程的参数
                                    if (log.isDebugEnabled()){
                                        log.debug("Haven built the connection successfully with:{}",address);
                                    }
                                    channelFuture.complete(promise.channel());
                                } else if (!promise.isSuccess()) {
                                    channelFuture.completeExceptionally(promise.cause());
                                }
                            });
                    //阻塞获取channel
                    channel = channelFuture.get(3, TimeUnit.SECONDS);
                    //缓存
                    ElephantRPCBootstrap.CHANNEL_CACHE.put(address,channel);
                }
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
                channel.writeAndFlush(new Object()).addListener((ChannelFutureListener)promise ->{
                    //当前的 promise 将来返回的结果是什么 --- writeAndFlush的返回结果
                    //一旦数据被写出去，这个 promise 也就结束了
                    //但是想要的是服务端给的返回值，completableFuture.complete(promise.getNow());不能这样写。
                    //应该将 completableFuture 挂起并暴露，并且在得到服务提供方响应时调用 complete 方法
                    //TODO completableFuture 挂起并暴露
//                    if(promise.isDone()){
//                        completableFuture.complete(promise.getNow());
//                    }
                    //所以只需要处理异常就行了
                    if (!promise.isSuccess()){
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });
                return completableFuture.get(3,TimeUnit.SECONDS);

                //TODO 线程池关闭
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

}
