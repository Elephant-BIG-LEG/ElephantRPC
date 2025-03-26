package com.elephant.loadbalancer.impl;

import com.elephant.YrpcBootstrap;
import com.elephant.exception.LoadBalancerException;
import com.elephant.loadbalancer.AbstractLoadBalancer;
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
 * @Description: 轮询负载均衡器
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements Selector {
        // TODO 这里的 serviceList 还必须修改，即使刷新了全局的服务列表，这里的 serviceList 还是使用的是缓存中的数据
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        /**
         * 轮询算法
         * @return 可用服务地址
         */
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

    }
}
