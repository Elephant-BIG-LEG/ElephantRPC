package com.elephant.discovery;

import com.elephant.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/07/20:46
 * @Description: 抽象注册中心接口
 */
public interface Registry {
    //功能：发现服务、注册服务

    /**
     * 注册服务
     * @param serviceConfig 服务配置内容
     */
    public void registry(ServiceConfig<?> serviceConfig);

    /**
     * 寻找一个可用服务
     * @param name 服务名称
     * @return URL【IP + PORT】
     */
    InetSocketAddress searchService(String name);
}
