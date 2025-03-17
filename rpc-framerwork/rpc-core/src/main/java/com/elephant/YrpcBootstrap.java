package com.elephant;


import com.elephant.discovery.AbstractRegistry;
import com.elephant.discovery.Registry;
import com.elephant.discovery.RegistryConfig;
import com.elephant.discovery.impl.ZookeeperRegistry;
import com.elephant.utils.NetUtils;
import com.elephant.utils.Zookeeper.ZookeeperNode;
import com.elephant.utils.Zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/14:41
 * @Description: 启动核心类
 */
@Slf4j
public class YrpcBootstrap<T> {

    /**
     * --------------------------- 相关的基础配置 --------------------------------
     */
    private static String appName = "default";

    private RegistryConfig registryConfig;

//    //维护一个 Zookeeper 实例 保证只启用一个
//    private ZooKeeper zookeeper;

    //维护一个注册中心
    private Registry registry;

    //当服务调用方，通过接口、方法名、具体的方法参数列表发起调用，提供怎么知道使用哪一个实现
    // (1) new 一个  （2）spring beanFactory.getBean(Class)  (3) 自己维护映射关系
    // 维护已经发布且暴露的服务列表 key-> interface的全限定名  value -> ServiceConfig
    public final static Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);

    /**
     * --------------------------- 服务提供方相关 API --------------------------------
     */

    private static final YrpcBootstrap yrpcBootstrap = new YrpcBootstrap();

    private YrpcBootstrap() {

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static YrpcBootstrap getInstance() {
        return yrpcBootstrap;
    }


    public YrpcBootstrap application(String appName) {
        log.info("为该服务起一个名字：{}", appName);
        this.appName = appName;
        return this;
    }


    public YrpcBootstrap registry(RegistryConfig registryConfig) {
        log.info("开始注册该服务：{}", registryConfig);
        //使用模板方法
        //true 表示使用默认配置
        this.registry = registryConfig.getRegistry(true);
        return this;

    }

    /**
     * 发布服务提供方的相关节点
     * @param service
     * @return
     */
    public YrpcBootstrap publish(ServiceConfig<T> service) {
        //抽象注册中心的概念
        registry.register(service);
        return this;
    }


    public void start() {
        log.info("服务提供方启用");
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * --------------------------- 服务调用端相关 API --------------------------------
     */

    public YrpcBootstrap reference(ReferenceConfig<?> reference) {
        log.info("通过核心配置类去完善服务调用端的配置类");
        return null;
    }



}
