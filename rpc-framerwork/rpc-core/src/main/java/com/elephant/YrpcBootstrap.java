package com.elephant;


import com.elephant.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.server.ServerConfig;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/14:41
 * @Description: 启动核心类
 */
@Slf4j
public class YrpcBootstrap {
    /**
     * --------------------------- 服务提供方相关 API --------------------------------
     */

    private static final YrpcBootstrap yrpcBootstrap = new YrpcBootstrap();

    private YrpcBootstrap() {

    }

    /**
     * 获取单例
     * @return
     */
    public static YrpcBootstrap getInstance() {
        return yrpcBootstrap;
    }


    public YrpcBootstrap application(String connectString) {
        log.info("为该服务起一个名字：{}",connectString);
        return this;
    }


    public YrpcBootstrap registry(RegistryConfig registryConfig) {
        log.info("开始注册该服务：{}",registryConfig);
        //TODO 注册该服务
        return this;

    }

    public YrpcBootstrap publish(ServerConfig service){
        log.info("发布服务");
        return null;
    }


    public void start() {
        log.info("服务提供方启用");
    }

    /**
     * --------------------------- 服务调用端相关 API --------------------------------
     */

    public YrpcBootstrap reference(ReferenceConfig<?> reference) {
        log.info("通过核心配置类去完善服务调用端的配置类");
        return null;
    }


}
