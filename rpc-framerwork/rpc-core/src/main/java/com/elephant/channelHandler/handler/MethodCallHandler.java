package com.elephant.channelHandler.handler;

import com.elephant.ServiceConfig;
import com.elephant.YrpcBootstrap;
import com.elephant.core.ShutDownHolder;
import com.elephant.enumeration.RequestType;
import com.elephant.enumeration.RespCode;
import com.elephant.protection.RateLimiter;
import com.elephant.protection.TokenBuketRateLimiter;
import com.elephant.transport.message.RequestPayload;
import com.elephant.transport.message.YrpcRequest;
import com.elephant.transport.message.YrpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/21/15:06
 * @Description: 反射处理服务调用端发送的数据
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<YrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, YrpcRequest yrpcRequest) throws Exception {


        // 1、先封装部分响应
        YrpcResponse yrpcResponse = new YrpcResponse();
        yrpcResponse.setRequestId(yrpcRequest.getRequestId());
        yrpcResponse.setCompressType(yrpcRequest.getCompressType());
        yrpcResponse.setSerializeType(yrpcRequest.getSerializeType());

        // 2、 获得通道
        Channel channel = channelHandlerContext.channel();

        if(ShutDownHolder.BAFFLE.get()){
            yrpcResponse.setBody(RespCode.CLOSING.getCode());
            channel.writeAndFlush(yrpcResponse);
            return ;
        }

        // 计数器 + 1
        ShutDownHolder.REQUEST_COUNTER.increment();


        // 4、完成限流相关的操作
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter =
                YrpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();

        // 获取限流器
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBuketRateLimiter(10, 10);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();

        // 5、处理请求的逻辑
        if (!allowRequest) {
            /** -------------------限流------------------**/
            log.info("开始限流");
            // 需要封装响应并且返回了
            yrpcResponse.setCode(RespCode.RATE_LIMIT.getCode());

        } else if (yrpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()) {
            /** -------------------心跳------------------**/
            // 需要封装响应并且返回
            log.info("处理心跳");
            yrpcResponse.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());

        } else {
            /** ---------------具体的调用过程--------------**/
            log.info("不需要进行限流，进行具体调用");
            // （1）获取负载内容
            RequestPayload requestPayload = yrpcRequest.getRequestPayload();
            // （2）根据负载内容进行方法调用
            try {
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用。", yrpcRequest.getRequestId());
                }
                // （3）封装响应   我们是否需要考虑另外一个问题，响应码，响应类型
                yrpcResponse.setCode(RespCode.SUCCESS.getCode());
                yrpcResponse.setBody(result);
            } catch (Exception e){
                log.error("编号为【{}】的请求在调用过程中发生异常。",yrpcRequest.getRequestId(),e);
                yrpcResponse.setCode(RespCode.FAIL.getCode());
            }
        }

         //6、写出响应
        channel.writeAndFlush(yrpcResponse);

        // 7、计数器减一
        ShutDownHolder.REQUEST_COUNTER.decrement();


    }

    /**
     * 根据客户端发送的请求负载（RequestPayload），通过反射调用目标服务的方法，并返回方法的执行结果
     * @param requestPayload
     * @return
     */
    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        // 寻找到匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = YrpcBootstrap.SERVERS_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        // 通过反射调用 1、获取方法对象  2、执行invoke方法
        Object returnValue;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("调用服务【{}】的方法【{}】时发生了异常。", interfaceName, methodName, e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
