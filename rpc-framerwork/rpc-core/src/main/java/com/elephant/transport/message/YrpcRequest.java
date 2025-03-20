package com.elephant.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/20/20:14
 * @Description: 请求封装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YrpcRequest {

    // 请求的id
    private long requestId;

    // 请求的类型，压缩的类型，序列化的方式
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    private long timeStamp;

    // 具体的消息体
    private RequestPayload requestPayload;


}