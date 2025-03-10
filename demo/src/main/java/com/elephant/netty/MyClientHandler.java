package com.elephant.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/04/22:15
 * @Description: TODO
 */
public class MyClientHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        Channel channel = ctx.channel();
        System.out.println(ctx.channel().writeAndFlush(Unpooled.copiedBuffer(
                "hello server".getBytes(StandardCharsets.UTF_8))));
        System.out.println(byteBuf.toString(StandardCharsets.UTF_8));
        System.out.println(channel.remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
