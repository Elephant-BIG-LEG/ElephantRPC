package com.elephant.discovery;

import com.elephant.ElephantRPCBootstrap;
import com.elephant.exception.NetworkException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //TODO 动态添加 handler 问题
                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
                                //服务提供方给与的结果
                                String result = msg.toString(Charset.defaultCharset());
                                //同全局的挂起的请求中寻找与之匹配的待处理的 CompletableFuture 进行关联
                                CompletableFuture<Object> completableFuture = ElephantRPCBootstrap.PENDING_REQUEST.get(1L);
                                completableFuture.complete(result);
                            }
                        });
                    }
                });
        if(result == null){
            throw new NetworkException("**** Failed acquire the channel");
        }
        return result;
    }
}
