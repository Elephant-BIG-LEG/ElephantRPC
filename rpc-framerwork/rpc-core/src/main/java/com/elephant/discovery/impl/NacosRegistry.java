package com.elephant.discovery.impl;

import com.elephant.ServiceConfig;
import com.elephant.discovery.AbstractRegistry;
import com.elephant.utils.Zookeeper.ZookeeperUtils;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/17/16:22
 * @Description: TODO
 */
public class NacosRegistry extends AbstractRegistry {

    public NacosRegistry() {
        //TODO
    }
    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        return null;
    }

}
