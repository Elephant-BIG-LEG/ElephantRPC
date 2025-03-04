package com.elephant;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/03/20:57
 * @Description: 零拷贝测试
 */
public class NettyTest {
    //使用 CompositeByteBuf 零拷贝 http 请求
    //在一个完整的 http 请求中，通常会被分散为多个 Buffer 中，用 CompositeByteBuf 将多个 Buffer 组合在一起，而无需额外的复制
    public void compositeByteBufTest(){
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();
        CompositeByteBuf httpBuf = Unpooled.compositeBuffer();
        httpBuf.addComponents(httpBuf,body);
    }

    public void jdkBuffer(){
        //相比与 JDK 的 Netty 实现更合理，省去了不必要的内存复制，可以称得上是 JVM 层面的零拷贝
        //JDK 的 Netty 实现中，ByteBuf 和 ByteBuffer 之间需要进行内存复制，而 JDK 的 Buffer 不需要
        ByteBuffer header = ByteBuffer.allocate(1024);
        ByteBuffer body = ByteBuffer.allocate(1024);
        ByteBuffer httpBuffer = ByteBuffer.allocate(header.remaining() + body.remaining());

        httpBuffer.put(header);
        httpBuffer.put(body);
        //切换模式
        httpBuffer.flip();
    }

    public void wrapBuffer(){
        //使用 wrap 方法将 byte 数组 包装成 ByteBuffer，不需要额外的内存复制
        byte[] bytes = new byte[1024];
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(bytes);

        //以上的操作还有一次额外的拷贝
        //Unpooled.wrappedBuffer 方法来将 bytes 包装成为一个 UnpooledHeapByteBuf 对象, 而在包装的过程中, 是
        //不会有拷贝操作的. 即最后我们生成的生成的 ByteBuf 对象是和 bytes 数组共用了同一个存储空间, 对 bytes 的修改也会反映到 ByteBuf 对象中
        byte[] bytes1 = new byte[1024];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes1);
    }

    public void sliceBuffer(){
        //使用 slice 方法将 ByteBuf 切.跟 wrap方法相反
        //产生 byteBuf 的过程是没有拷贝操作的，header 和body 对象在内部其实是共i想了 byteBuf 存储空间的不同部分而已
       ByteBuf byteBuf = Unpooled.buffer();
       ByteBuf header = byteBuf.slice(0,5);
       ByteBuf body = byteBuf.slice(0,5);
    }

}
