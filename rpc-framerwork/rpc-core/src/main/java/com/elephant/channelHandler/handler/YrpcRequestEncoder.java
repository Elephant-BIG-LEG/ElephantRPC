package com.elephant.channelHandler.handler;


import com.elephant.compress.Compressor;
import com.elephant.compress.CompressorFactory;
import com.elephant.enumeration.RequestType;
import com.elephant.serialize.Serializer;
import com.elephant.serialize.SerializerFactory;
import com.elephant.transport.message.MessageFormatConstant;
import com.elephant.transport.message.RequestPayload;
import com.elephant.transport.message.YrpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 自定义协议编码器
 * <p>
 * <pre>
 *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *   |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 *   +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 *   |                                                                                                             |
 *   |                                         body                                                                |
 *   |                                                                                                             |
 *   +--------------------------------------------------------------------------------------------------------+---+
 * </pre>
 * <p>
 * 4B magic(魔数)   --->yrpc.getBytes()
 * 1B version(版本)   ----> 1
 * 2B header length 首部的长度
 * 4B full length 报文总长度
 * 1B serialize
 * 1B compress
 * 1B requestType
 * 8B requestId
 * <p>
 * body
 * <p>
 * 出站时，第一个经过的处理器
 *
 * @author Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Description: 服务调用方编码
 */
@Slf4j
public class YrpcRequestEncoder extends MessageToByteEncoder<YrpcRequest> {
    
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, YrpcRequest yrpcRequest, ByteBuf byteBuf) throws Exception {
        // 4个字节的魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2个字节的头部的长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 总长度不清楚，不知道body的长度 writeIndex(写指针)
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 3个类型
        byteBuf.writeByte(yrpcRequest.getRequestType());
        byteBuf.writeByte(yrpcRequest.getSerializeType());
        byteBuf.writeByte(yrpcRequest.getCompressType());
        // 8字节的请求id
        byteBuf.writeLong(yrpcRequest.getRequestId());
        byteBuf.writeLong(yrpcRequest.getTimeStamp());

        // 如果是心跳请求，就不处理请求体
        if(yrpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()){
            // 处理一下总长度，其实总长度 = header长度
            int writerIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH
            );
            byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH);
            byteBuf.writerIndex(writerIndex);
            return;
        }
        
        // 写入请求体（requestPayload）
        // 1、根据配置的序列化方式进行序列化
        // 怎么实现序列化 1、工具类 耦合性很高 如果以后我想替换序列化的方式，很难
        byte[] body = null;
        if (yrpcRequest.getRequestPayload() != null) {
            Serializer serializer = SerializerFactory.getSerializer(yrpcRequest.getSerializeType()).getImpl();
            body = serializer.serialize(yrpcRequest.getRequestPayload());
            // 2、根据配置的压缩方式进行压缩
            Compressor compressor = CompressorFactory.getCompressor(yrpcRequest.getCompressType()).getImpl();
            body = compressor.compress(body);
        }

        if (body != null) {
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;

        // 重新处理报文的总长度
        // 先保存当前的写指针的位置
        int writerIndex = byteBuf.writerIndex();
        // 将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH
        );
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        // 将写指针归位
        byteBuf.writerIndex(writerIndex);

        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已经完成报文的编码。", yrpcRequest.getRequestId());
        }
    }

    /**
     * TODO 这样解析有问题
     * 将对象转成一个字节数组
     * @param requestPayload
     * @return
     */
    private byte[] getBodyBytes(RequestPayload requestPayload){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(requestPayload);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    
    
}
