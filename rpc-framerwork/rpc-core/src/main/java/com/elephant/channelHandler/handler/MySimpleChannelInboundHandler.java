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
 * @Description: 服务调用端根据响应码处理不同的响应逻辑
 * @Function：响应处理、错误处理、心跳处理、服务关闭处理、断路器记录、负载均衡重新分配
 *            响应处理：根据响应码处理不同的响应逻辑。
 *            错误处理：记录错误信息，并通过 CompletableFuture 通知调用方。
 *            心跳处理：处理心跳响应。
 *            服务关闭处理：处理服务端正在关闭的情况。
 *            断路器记录：记录错误请求，用于断路器机制。
 *            负载均衡重新分配：在服务端关闭时，重新分配负载均衡。
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
