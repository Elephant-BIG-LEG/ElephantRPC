package com.elephant.channelHandler;

import com.elephant.channelHandler.handler.ElephantRPCMessageEncoder;
import com.elephant.channelHandler.handler.MySimpleChanelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/09/10:45
 * @Description: TODO
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                //添加 netty 自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                //消息编码器
                .addLast(new ElephantRPCMessageEncoder())
                //出栈
                .addLast(new MySimpleChanelInboundHandler());
    }


}
