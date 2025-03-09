package com.elephant.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/09/11:08
 * @Description: 服务调用方发起的请求消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElephantRPCRequest {
    //请求的 ID
    private long requestId;

    //请求的类型，压缩的类型，序列化的方式
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    //具体的消息体【接口的名字、方法的名字、参数列表】
    private RequestPayload requestPayload;
}
