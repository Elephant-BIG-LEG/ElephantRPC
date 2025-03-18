package com.elephant;

import com.elephant.discovery.Registry;
import com.elephant.discovery.RegistryConfig;
import com.elephant.exception.DiscoveryException;
import com.elephant.exception.NetworkException;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
                InetSocketAddress FirstAddress  = addresses.get(0);
                log.info("选择了该服务：【{}】的【{}】节点",interfaceRef.getName(),FirstAddress);


                //2.使用 Netty 发起 RPC 调用
                //应该将 Netty 的连接进行缓存，每一次建立一个新的连接是不合理的
                Channel channel = YrpcBootstrap.CHANNEL_CACHE.get(FirstAddress);
                if(channel == null){
                    //建立连接
                    NioEventLoopGroup group = new NioEventLoopGroup();
                    try {
                        Bootstrap bootstrap = new Bootstrap();
                        //引导程序
                        bootstrap = bootstrap.group(group)
                                .remoteAddress(new InetSocketAddress(8080))
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        //TODO
                                        socketChannel.pipeline().addLast(null);
                                    }
                                });
                        //尝试连接
                        channel = bootstrap.connect(FirstAddress).sync().channel();
                        YrpcBootstrap.CHANNEL_CACHE.put(FirstAddress,channel);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(channel == null){
                    //await 方法会阻塞，会等待连接成功再返回【需要考虑超时问题】，Netty 也提供了异步处理的逻辑
                    channel = NettyBootstrapInitializer.getBootstrap().connect(FirstAddress).await().channel();
                    YrpcBootstrap.CHANNEL_CACHE.put(FirstAddress,channel);
                }

                if(channel == null){
                    throw new NetworkException("获取通道异常");
                }
                //发送请求
                ChannelFuture channelFuture = channel.writeAndFlush(new Object());

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
