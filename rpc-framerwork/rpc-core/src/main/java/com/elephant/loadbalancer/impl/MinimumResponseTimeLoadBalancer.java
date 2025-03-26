package com.elephant.loadbalancer.impl;

import com.elephant.YrpcBootstrap;
import com.elephant.loadbalancer.AbstractLoadBalancer;
import com.elephant.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/26/8:44
 * @Description: 最短响应时间负载器
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }

    public static class MinimumResponseTimeSelector implements Selector{

        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = YrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();

            // 为什么这里要判空？就是应该 TreeMap 的判断总是慢半拍，还没统计完就直接调用
            if(entry != null){
                if(log.isDebugEnabled()){
                    log.debug("选取了一个响应时间为：【{}】的服务地址",entry.getKey(),entry.getValue().remoteAddress());
                    return (InetSocketAddress) entry.getValue().remoteAddress();
                }
            }
            // 不存在，直接从缓存中获取
            Channel channel = (Channel) YrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }
    }


}
