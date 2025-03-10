package com.elephant.channelHandler.handler;

import com.elephant.enumeration.RequestType;
import com.elephant.transport.message.ElephantRPCRequest;
import com.elephant.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import java.util.Random;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/09/15:10
 * @Description: 基于长度字段的解码器
 */
@Slf4j
public class ElephantRPCMessageDecoder extends LengthFieldBasedFrameDecoder {


    public ElephantRPCMessageDecoder() {
        super(
                // 找到当前报文的总长度，截取报文，截取出来的报文我们可以去进行解析
                // 最大帧的长度，超过这个maxFrameLength值会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度的字段的偏移量，
                MessageFormatConstant.MAGIC.length
                        + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // 长度的字段的长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                // todo 负载的适配长度 这里设置出现了问题
                -(MessageFormatConstant.MAGIC.length
                        + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH
                        + MessageFormatConstant.FULL_FIELD_LENGTH),
                0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
//        System.out.println(MessageFormatConstant.MAGIC.length
//                + MessageFormatConstant.VERSION_LENGTH
//                + MessageFormatConstant.HEADER_FIELD_LENGTH);
//        System.out.println(MessageFormatConstant.FULL_FIELD_LENGTH);
//        System.out.println(MessageFormatConstant.MAGIC.length
//                + MessageFormatConstant.VERSION_LENGTH
//                + MessageFormatConstant.HEADER_FIELD_LENGTH
//                + MessageFormatConstant.FULL_FIELD_LENGTH);

        Thread.sleep(new Random().nextInt(50));


        //TODO P0事故 解析报文出错!!!
        //进行解析
        Object decode = super.decode(ctx, in);

        if(decode instanceof ByteBuf byteBuf){
            return decodeFrame(byteBuf);
        }

        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 1、解析魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        // 检测魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("**** The request obtained is not legitimate。");
            }
        }

        // 2、解析版本号
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("**** The obtained request version is not supported.");
        }

        // 3、解析头部的长度
        short headLength = byteBuf.readShort();

        // 4、解析总长度
        int fullLength = byteBuf.readInt();

        // 5、请求类型
        byte requestType = byteBuf.readByte();

        // 6、序列化类型
        byte serializeType = byteBuf.readByte();

        // 7、压缩类型
        byte compressType = byteBuf.readByte();

        // 8、请求id
        long requestId = byteBuf.readLong();

        // 9、时间戳
        long timeStamp = byteBuf.readLong();

        // 我们需要封装
        ElephantRPCRequest elephantRPCRequest = new ElephantRPCRequest();
        elephantRPCRequest.setRequestType(requestType);
        elephantRPCRequest.setCompressType(compressType);
        elephantRPCRequest.setSerializeType(serializeType);
        elephantRPCRequest.setRequestId(requestId);
//        elephantRPCRequest.setTimeStamp(timeStamp);

        // 心跳请求没有负载，此处可以判断并直接返回
        if( requestType == RequestType.HEART_BEAT.getId()){
            return elephantRPCRequest;
        }

        //请求报文
        int payloadLength = fullLength - headLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

//        // 有了字节数组之后就可以解压缩，反序列化
//        // 1、解压缩
//        if(payload != null && payload.length != 0) {
//            Compressor compressor = CompressorFactory.getCompressor(compressType).getImpl();
//            payload = compressor.decompress(payload);
//
//
//            // 2、反序列化
//            Serializer serializer = SerializerFactory.getSerializer(serializeType).getImpl();
//            RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
//            yrpcRequest.setRequestPayload(requestPayload);
//        }

        if(log.isDebugEnabled()){
            log.debug("请求【{}】已经在服务端完成解码工作。",elephantRPCRequest.getRequestId());
        }

        System.out.println("---------------------------");
        System.out.println("---------------------------");
        System.out.println(elephantRPCRequest.toString());
        System.out.println("---------------------------");
        System.out.println("---------------------------");
        return elephantRPCRequest;
    }
}
