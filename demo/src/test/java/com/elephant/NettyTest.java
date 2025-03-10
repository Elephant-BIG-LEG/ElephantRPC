package com.elephant;

import com.elephant.netty.client;
import io.netty.buffer.*;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    /**
     * 封装请求报文
     * @throws IOException
     */
    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();

        message.writeBytes("elephant".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);

        //用对象流转化为字节数据
        client client = new client();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(client);
        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);
        //打印
        printAsBinary(message);
    }
    public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i += 2) {
            formattedBinary.append(binaryString.substring(i, i + 2)).append(" ");
        }
        System.out.println("Binary representation: " + formattedBinary.toString());
    }

    //解压
    @Test
    public void testDeCompress() throws  IOException{
        byte[] buf = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, 99, 102, 102, 102, 6, 0, -46, -52, -109, -125, 4, 0, 0, 0};

        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);

        byte[] bytes = gzipInputStream.readAllBytes();
        System.out.println(buf.length + " -> " + bytes.length);
        System.out.println(Arrays.toString(bytes));
    }

    //加密
    @Test
    public void testEncrypt() throws  IOException{
        byte[] buf = new byte[]{3,3,3,3};

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);


        gzipOutputStream.write(buf);
        gzipOutputStream.finish();
        byte[] bytes = baos.toByteArray();

        System.out.println(buf.length + " -> " + bytes.length);
        System.out.println(Arrays.toString(bytes));
    }




}
