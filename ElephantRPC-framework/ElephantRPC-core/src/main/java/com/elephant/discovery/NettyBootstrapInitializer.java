package com.elephant.discovery;

import com.elephant.channelHandler.ConsumerChannelInitializer;
import com.elephant.channelHandler.handler.MySimpleChanelInboundHandler;
import com.elephant.exception.NetworkException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/08/18:57
 * @Description: Netty 连接封装类
 */
@Slf4j
public class NettyBootstrapInitializer {

    //启动一个 Netty 需要一个辅助类
    private static final Bootstrap bootstrap = new Bootstrap();

    //建立新的 Netty 连接
    private static NioEventLoopGroup group = new NioEventLoopGroup();
    public NettyBootstrapInitializer() {

    }

    /**
     * 获取一个 Netty 的 Bootstrap
     * 确保了不用每次创建的时候都需要新建连接 --- 饿汉式
     * @return bootstrap 对象
     */
    public static Bootstrap getBootstrap() {
        Bootstrap result = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
        if(result == null){
            throw new NetworkException("**** Failed acquire the channel");
        }
        return result;
    }
}
