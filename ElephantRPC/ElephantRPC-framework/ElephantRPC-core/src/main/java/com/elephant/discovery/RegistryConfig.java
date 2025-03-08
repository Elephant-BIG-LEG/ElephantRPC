package com.elephant.discovery;

import com.elephant.Constant;
import com.elephant.discovery.impl.NacosRegistry;
import com.elephant.discovery.impl.ZookeeperRegistry;
import com.elephant.exception.DiscoveryException;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/21:36
 * @Description: TODO
 */
public class RegistryConfig {
    //定义连接的 URL
    private final String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 通过 connectString 获取一个注册中心
     * @return 返回具体的注册中心实例
     */
    public Registry getRegistry() {
        //获取注册中心的类型
        String registryType = getRegistryType(connectString,true).toLowerCase().trim();
        //连接字符串
        String host = getRegistryType(connectString, false);
        if(registryType.equals(Constant.ZOOKEEPER_TYPE)){
            return new ZookeeperRegistry(host,Constant.TIME_OUT);
        } else if (registryType.equals("nacos")) {
            return new NacosRegistry(host,Constant.TIME_OUT);
        }
        throw new DiscoveryException("**** Create the registry is failed!!!");
    }

    /**
     * 使用简单工厂来完成连接 URL 与注册中心的映射
     * @param connectString
     * @return
     */
    private String getRegistryType(String connectString,boolean ifType){
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length != 2){
            throw new RuntimeException("**** The URL is illegal!!!");
        }
        if(ifType){
            return typeAndHost[0];
        }else {
            return typeAndHost[1];
        }
    }
}
