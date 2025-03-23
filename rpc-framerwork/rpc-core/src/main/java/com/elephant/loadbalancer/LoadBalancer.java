package com.elephant.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/23/11:04
 * @Description: TODO
 */
public interface LoadBalancer {

    /**
     * 根据服务名称选择可用服务
     * @param serviceName 服务名称
     * @param group 分组信息
     * @return 可用一个服务地址
     */
    InetSocketAddress selectServiceAddress(String serviceName,String group);
}
