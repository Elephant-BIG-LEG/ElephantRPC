package com.elephant.watcher;

import com.elephant.NettyBootstrapInitializer;
import com.elephant.YrpcBootstrap;
import com.elephant.discovery.Registry;
import com.elephant.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/26/10:36
 * @Description: 发现服务提供方的动态上下线
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if(event.getType() == Event.EventType.NodeChildrenChanged){
            if(log.isDebugEnabled()){
                log.debug("检测到服务：【{}】的子节点发生变化,重新 上 / 下线的工作",event.getPath());
            }
            String serviceName = getServiceName(event.getPath());
            Registry registry = YrpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addresses = registry.lookup(serviceName, null);
            // 处理新增的节点
            for (InetSocketAddress address : addresses) {
                // 新增的节点   会在address 不在CHANNEL_CACHE
                if(!YrpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap()
                                .connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    YrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }

            // 处理下线节点
            for (Map.Entry<InetSocketAddress,Channel> entry : YrpcBootstrap.CHANNEL_CACHE.entrySet()){
                if(!addresses.contains(entry.getKey())){
                    YrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            // TODO 这里得去刷新缓存中服务列表，让服务提供方即使发生了服务上下线也能够进行负载均衡
            // 获得负载均衡器，进行重新加载
            LoadBalancer loadBalancer = YrpcBootstrap.configuration.getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName,addresses);

        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
