package com.elephant.discovery;

import com.elephant.Constants;
import com.elephant.discovery.impl.NacosRegistry;
import com.elephant.discovery.impl.ZookeeperRegistry;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/15:39
 * @Description: 注册中心核心类
 */
public class RegistryConfig {
    private String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    //TODO 发现服务、注册服务、下线服务

    public Registry getRegistry(boolean isDefault){
        String registryType = getRegistryType(connectString,true).toLowerCase().trim();
        String host = getRegistryType(connectString,false);
        //TODO 这里使用默认配置 如果后期使用 UI 管理界面可以传入 boolean 参数
        switch (registryType){
            case "zookeeper":
                return new ZookeeperRegistry(connectString,host,isDefault);
            case "nacos":
                return new NacosRegistry();
            default:
                throw new IllegalArgumentException("为找到可用的服务设配器");
        }
    }

    /**
     * @param connectString
     * @param ifType
     * @return 返回注册中心类型 或者 URL
     */
    private String getRegistryType(String connectString,boolean ifType) {
        String[] split = connectString.toLowerCase().split("://");
        if(ifType){
            return split[0];
        }else{
            return split[1];
        }
    }

}
