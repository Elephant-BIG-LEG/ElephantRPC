package com.elephant.channelHandler;

import com.elephant.channelHandler.handler.MySimpleChannelInboundHandler;
import com.elephant.channelHandler.handler.YrpcRequestEncoder;
import com.elephant.channelHandler.handler.YrpcResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/20/15:15
 * @Description: 整合服务调用端处理器
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                // netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 消息编码器
                .addLast(new YrpcRequestEncoder())
                // 入栈的解码器
                .addLast(new YrpcResponseDecoder())
                // 处理结果
                .addLast(new MySimpleChannelInboundHandler());
    }
}
