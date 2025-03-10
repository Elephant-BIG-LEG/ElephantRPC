package com.elephant.channelHandler.handler;

import com.elephant.enumeration.RequestType;
import com.elephant.transport.message.ElephantRPCRequest;
import com.elephant.transport.message.MessageFormatConstant;
import com.elephant.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/09/11:41
 * @Description: 出栈时的第一个处理器
 *              4bit magic(魔数） --- ERPC.getByte()
 *              1bit version(版本) --- 1
 *              2bit header length(首部的长度)
 *                                       ----- 解决沾包、黏包
 *              4bit full length(报文总长度)
 *              1bit serialize(序列化) --- 1
 *              1bit compress
 *              1bit requestType
 *              8bit requestId
 */
public class ElephantRPCMessageEncoder extends MessageToByteEncoder<ElephantRPCRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ElephantRPCRequest elephantRPCRequest, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //头部长度
        byteBuf.writeByte(MessageFormatConstant.HEADER_FIELD_LENGTH);
        //TODO 总长度未知 移动写指针
        byteBuf.writeInt(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        //3个类型
        byteBuf.writeByte(elephantRPCRequest.getRequestType());
        byteBuf.writeByte(elephantRPCRequest.getSerializeType());
        byteBuf.writeByte(elephantRPCRequest.getCompressType());
        //8字节的请求 id
        byteBuf.writeLong(elephantRPCRequest.getRequestId());

        //心跳检测直接返回
//        if (elephantRPCRequest.getRequestType() == RequestType.HEART_BEAT.getId()){
//            //处理一下总长度，其实是总长度就等于 header 长度
//            int writerIndex = byteBuf.writerIndex();
//            byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
//                    + MessageFormatConstant.VERSION_LENGTH
//                    + MessageFormatConstant.HEADER_FIELD_LENGTH);
//            byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH);
//            //将写指针归位
//            byteBuf.writerIndex(writerIndex);
//            return;
//        }

        //写入请求体（requestPayload)
        byte[] body = getBodyBytes(elephantRPCRequest.getRequestPayload());
        if (body != null){
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;

        //重新处理报文的总长度
        //先保存写指针的位置
        int writerIndex = byteBuf.writerIndex();
        //将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH
                + MessageFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        //将写指针归位
        byteBuf.writerIndex(writerIndex);
    }

    private byte[] getBodyBytes(RequestPayload requestPayload) {
        //TODO 针对不同的消息类型需要做不同的处理，心跳的请求，没有payload
        if (requestPayload == null){
            return null;
        }
        //对象怎么变成一个字节数组
        //序列化
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(requestPayload);

            //压缩


            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
