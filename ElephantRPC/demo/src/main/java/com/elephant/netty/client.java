package com.elephant.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/04/21:14
 * @Description: TODO
 */
public class client {
    public static void main(String[] args) {
        new client().run();
    }

    public void run() {
        NioEventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        try {
        bootstrap.group(group)
                .remoteAddress(new InetSocketAddress(8080))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MyClientHandler());
                    }
                });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello".getBytes(StandardCharsets.UTF_8)));

            //只有正常关闭才能触发关闭
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            System.out.println(e);
        }finally {
            try {
                group.shutdownGracefully();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    }
}
