package com.elephant.channelHandler.handler;

import com.elephant.YrpcBootstrap;
import com.elephant.enumeration.RespCode;
import com.elephant.exception.ResponseException;
import com.elephant.protection.CircuitBreaker;
import com.elephant.transport.message.YrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/20/15:12
 * @Description: 服务调用端根据响应码处理不同的响应逻辑 返回给调用者
 * @Function：响应处理、错误处理、心跳处理、服务关闭处理、断路器记录、负载均衡重新分配
 *            响应处理：根据响应码处理不同的响应逻辑。
 *            错误处理：记录错误信息，并通过 CompletableFuture 通知调用方。
 *            心跳处理：处理心跳响应。
 *            服务关闭处理：处理服务端正在关闭的情况。
 *            断路器记录：记录错误请求，用于断路器机制。
 *            负载均衡重新分配：在服务端关闭时，重新分配负载均衡。
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<YrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, YrpcResponse yrpcResponse) throws Exception {

        //从全局挂起的请求中，匹配请求 ID，找到 completableFuture
        CompletableFuture<Object> completableFuture =
                YrpcBootstrap.PENDING_REQUEST.get(yrpcResponse.getRequestId());
        // IP
        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        // 熔断器
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = YrpcBootstrap.getInstance()
                .getConfiguration().getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);

        byte code = yrpcResponse.getCode();

        /**
         * SUCCESS((byte) 20,"成功"),
         * SUCCESS_HEART_BEAT((byte) 21,"心跳检测成功返回"),
         * RATE_LIMIT((byte)31,"服务被限流" ),
         * RESOURCE_NOT_FOUND((byte)44,"请求的资源不存在" ),
         * FAIL((byte)50,"调用方法发生异常"),
         * BECOLSING((byte)51,"调用方法发生异常");
         */
        if(code == RespCode.FAIL.getCode()){
            // 异常
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，返回错误的结果，响应码[{}].",
                    yrpcResponse.getRequestId(),yrpcResponse.getCode());
            throw new ResponseException(code,RespCode.FAIL.getDesc());
        } else if (code == RespCode.SUCCESS.getCode()) {
            // 成功
            circuitBreaker.recordRequest();
            Object body = yrpcResponse.getBody();
            completableFuture.complete(body);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture，处理响应结果。", yrpcResponse.getRequestId());
            }
        } else if (code == RespCode.RATE_LIMIT.getCode()) {
            // 限流
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求，被限流，响应码【{}】.",
                    yrpcResponse.getRequestId(),yrpcResponse.getCode());
            throw new ResponseException(code,RespCode.RATE_LIMIT.getDesc());

        } else if (code == RespCode.RESOURCE_NOT_FOUND.getCode()) {
            // 没有该资源
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求，没有该资源，响应码【{}】.",
                    yrpcResponse.getRequestId(),yrpcResponse.getCode());
            throw new ResponseException(code,RespCode.RATE_LIMIT.getDesc());

        }  else if (code == RespCode.SUCCESS_HEART_BEAT.getCode()) {
            // 心跳检测
            circuitBreaker.recordRequest();
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture,处理心跳检测，处理响应结果。", yrpcResponse.getRequestId());
            }
        } else if (code == RespCode.BECLOSING.getCode()) {
            // 优雅停机

        }
    }


}
