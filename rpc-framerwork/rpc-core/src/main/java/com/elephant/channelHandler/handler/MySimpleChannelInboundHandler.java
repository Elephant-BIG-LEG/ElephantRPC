package com.elephant.channelHandler.handler;

import com.elephant.YrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/20/15:12
 * @Description: 服务调用端处理器的具体实现
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        //TODO 服务调用端处理数据
        //这是服务提供方发送的数据
        String result = msg.toString(Charset.defaultCharset());
        //从全局挂起的请求中，匹配请求 ID，找到 completableFuture
        //TODO 表示需要修改
        CompletableFuture<Object> completableFuture = YrpcBootstrap.PENDING_REQUEST.get(1L);
        //将结果设置到 CompletableFuture 中
        //                -- 这里执行成功便会处理 return completableFuture.get(10, TimeUnit.SECONDS);并返回结果
        completableFuture.complete(result);
    }
}
