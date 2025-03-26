package com.elephant.loadbalancer;

import com.elephant.YrpcBootstrap;
import com.elephant.discovery.Registry;
import com.elephant.exception.LoadBalancerException;
import com.elephant.loadbalancer.impl.RoundRobinLoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/23/15:19
 * @Description: TODO 应该是将服务调用端连接所有服务提供方，还是一个服务调用端只维护一部分的服务提供方呢？？？
 */
@Slf4j
public abstract class AbstractLoadBalancer implements LoadBalancer {


    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName, String group) {
        // 对于这个负载均衡器，内部应该维护服务列表作为缓存 -- TODO 需要修改服务列表【满足服务提供方上下线之后的负载均衡】
        Selector selector = cache.get(serviceName);
        // 提供一些算法负载选取合适的节点
        if (selector == null) {
            // 通过注册中心拉取服务 TODO 使用配置中心获取注册中心
            List<InetSocketAddress> serviceList = YrpcBootstrap.getRegistry().lookup(serviceName, group);
            // 选择负载均衡器
            selector = getSelector(serviceList);
            cache.put(serviceName, selector);
        }
        // 获取可用节点
        return selector.getNext();
    }

    /**
     * 根据新的服务列表生成新的 Selector【负载均衡器】
     * 需要保证线程安全
     *
     * @param serviceName 服务名称
     * @param addresses   最新的服务列表
     */
    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> addresses) {
        cache.put(serviceName, getSelector(addresses));
    }


    /**
     * 模板设计模式：获取一个Selector【负载均衡器】 由子类扩展【多种负载均衡算法】
     *
     * @param serviceList
     * @return 一个可用服务
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);


}

