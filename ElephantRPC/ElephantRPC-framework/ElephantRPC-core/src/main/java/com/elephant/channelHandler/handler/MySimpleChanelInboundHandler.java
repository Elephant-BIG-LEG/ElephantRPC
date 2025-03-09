package com.elephant.channelHandler.handler;

import com.elephant.ElephantRPCBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/09/10:42
 * @Description: 测试类
 */
public class MySimpleChanelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        //服务提供方给与的结果
        String result = msg.toString(Charset.defaultCharset());
        //同全局的挂起的请求中寻找与之匹配的待处理的 CompletableFuture 进行关联
        CompletableFuture<Object> completableFuture = ElephantRPCBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
