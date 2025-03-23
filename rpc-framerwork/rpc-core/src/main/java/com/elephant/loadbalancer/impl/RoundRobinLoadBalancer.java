package com.elephant.loadbalancer.impl;

import com.elephant.YrpcBootstrap;
import com.elephant.exception.LoadBalancerException;
import com.elephant.loadbalancer.LoadBalancer;
import com.elephant.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/23/11:04
 * @Description: TODO
 */
@Slf4j
public class RoundRobinLoadBalancer implements LoadBalancer {

    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName,String group) {
        // 对于这个负载均衡器，内部应该维护服务列表作为缓存
        Selector selector = cache.get(serviceName);
        // 提供一些算法负载选取合适的节点
        if (selector == null) {
            // 通过注册中心拉取服务
            List<InetSocketAddress> serviceList = YrpcBootstrap.getRegistry().lookup(serviceName,group);
            selector = new RoundRobinSelector(serviceList);
            cache.put(serviceName,selector);
        }
        // 获取可用节点
        return selector.getNext();
    }

    private static class RoundRobinSelector implements Selector {
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if(serviceList == null || serviceList.size() == 0){
                log.error("进行负载均衡选取节点时发现服务列表为空.");
                throw new LoadBalancerException();
            }
            InetSocketAddress address = serviceList.get(index.get());
            log.info("通过负载均衡获取到一个服务节点:【{}】",address);

            // 如果他到了最后的一个位置，轮询
            if(index.get() == serviceList.size() - 1){
                index.set(0);
            } else {
                // 游标后移一位
                index.incrementAndGet();
            }

            return address;
        }


        @Override
        public void reBalance() {

        }
    }
}
