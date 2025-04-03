package com.elephant.discovery;

import com.elephant.ServiceConfig;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/17/16:16
 * @Description: TODO
 */
public interface Registry {


    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取服务列表
     * @param serviceName 服务的名称
     * @return 服务的地址
     */
    List<InetSocketAddress> lookup(String serviceName, String group);
}
