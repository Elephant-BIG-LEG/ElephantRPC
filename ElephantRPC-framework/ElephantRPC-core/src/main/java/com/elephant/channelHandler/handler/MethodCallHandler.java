package com.elephant.channelHandler.handler;

import com.elephant.ElephantRPCBootstrap;
import com.elephant.ServiceConfig;
import com.elephant.transport.message.ElephantRPCRequest;
import com.elephant.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/09/17:14
 * @Description: TODO
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<ElephantRPCRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ElephantRPCRequest elephantRPCRequest) throws Exception {

        //获取负载内容
        RequestPayload requestPayload  = elephantRPCRequest.getRequestPayload();
        //根据负载内容进行调用
        Object object = callTargetMethod(requestPayload);

        //封装响应

        //写出响应
        channelHandlerContext.channel().writeAndFlush(null);


    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        // 寻找到匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = ElephantRPCBootstrap.SERVER_LIST.get(interfaceName);
        // 通过反射调用 1、获取方法对象  2、执行invoke方法
        Object refImpl = serviceConfig.getRef();
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
