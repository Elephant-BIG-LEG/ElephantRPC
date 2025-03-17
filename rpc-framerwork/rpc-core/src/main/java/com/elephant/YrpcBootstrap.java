package com.elephant;


import com.elephant.discovery.RegistryConfig;
import com.elephant.utils.NetUtils;
import com.elephant.utils.Zookeeper.ZookeeperNode;
import com.elephant.utils.Zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.server.ServerConfig;

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

    //维护一个 Zookeeper 实例 保证只启用一个
    private ZooKeeper zookeeper;

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
        zookeeper = ZookeeperUtils.createZookeeper();
        log.info("开始注册该服务：{}", registryConfig);
        //TODO 注册该服务
        this.registryConfig = registryConfig;
        return this;

    }

    /**
     * 发布服务提供方的相关节点
     * @param service
     * @return
     */
    public YrpcBootstrap publish(ServiceConfig<T> service) {
        String parentNode = Constants.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();
        //发布父节点【持久节点】
        if (!ZookeeperUtils.exists(zookeeper, parentNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            Boolean node = ZookeeperUtils.createNode(zookeeper, zookeeperNode, null, CreateMode.PERSISTENT);
            if (node) {
                log.info("成功发布持久节点服务：【{}】", service.getInterface().getName());
            }
        }
        //创建本机临时节点
        //发布服务提供方子节点【临时节点】
        String nodePath = parentNode + "/" + NetUtils.getIp() + ":" + Constants.PORT;
        if (!ZookeeperUtils.exists(zookeeper, nodePath, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(nodePath, null);
            Boolean node = ZookeeperUtils.createNode(zookeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
            if (node) {
                log.info("成功发布临时节点服务：【{}】", service.getInterface().getName());
            }
        }
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
