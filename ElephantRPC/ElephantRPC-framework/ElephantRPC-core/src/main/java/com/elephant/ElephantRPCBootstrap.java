package com.elephant;


import com.elephant.utils.NetUtils;
import com.elephant.utils.Zookeeper.ZookeeperNote;
import com.elephant.utils.Zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;


import java.util.List;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/21:22
 * @Description: 相关 API
 */
@Slf4j
public class ElephantRPCBootstrap {


    /**
     * ------------------------------------服务提供方的相关 API-----------------------------------
     */
    // ElephantRPCBootstrap 是个单例，希望每个应用只有一个实例 使用饿汉式
    private static ElephantRPCBootstrap elephantRPCBootstrap = new ElephantRPCBootstrap();

    //定义相关的一些配置
    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;

    private int port = 8088;

    //维护一个 zookeeper 实例
    private ZooKeeper zookeeper;

    private ElephantRPCBootstrap(){
        //构造启动引导程序，

    }
    public static ElephantRPCBootstrap getInstance() {
        return elephantRPCBootstrap;
    }

    /**
     * 用来定义当前应用的名字
     * @param appName 应用的名字
     * @return this 当前实例
     */
    public ElephantRPCBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 用来注册一个注册中心
     * @param registryConfig 注册中心
     * @return this 当前实例
     */
    public ElephantRPCBootstrap register(RegistryConfig registryConfig) {

        //TODO 为什么写在这里，而不是写在当前构造器中？？？ 这样写增加了耦合性
        //这里维护一个 zookeeper 实例，当时会将 zookeeper 和当前工程耦合
        //但是希望以后可以扩展更多不同的实现
        zookeeper = ZookeeperUtil.createZookeeper();


        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return this
     */
    public ElephantRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()){
            log.debug("当前工程使用了:{} 协议",protocolConfig.getProtocolName());
        }
        return this;
    }

    /**
     * ------------------------------------服务调用方的相关 API-----------------------------------
     */

    /**
     * 发布服务，将接口注册到服务中心
     * @param service 封装的是需要发布的服务
     * @return this
     */
    public ElephantRPCBootstrap publish(ServiceConfig<?> service) {
        //服务名称的节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        //这个节点应该是一个持久节点
        if (ZookeeperUtil.exists(zookeeper,parentNode,null)){
            ZookeeperNote zookeeperNote = new ZookeeperNote(parentNode,null);
            //持久节点
            ZookeeperUtil.createNode(zookeeper,zookeeperNote,null, CreateMode.PERSISTENT);
        }

        //创建 ** 本机 ** 的临时节点 格式: ip:port
        //服务提供方的端口，所以我们需要一个获取 ip 的方法
        //这个 IP 通常是需要一个局域网 ip
        String node = parentNode + "/" + NetUtils.getIp() + ":" + port;
        //判断该节点在不在
        if (ZookeeperUtil.exists(zookeeper,parentNode,null)){
            ZookeeperNote zookeeperNote = new ZookeeperNote(parentNode,null);
            //是临时节点
            ZookeeperUtil.createNode(zookeeper,zookeeperNote,null, CreateMode.EPHEMERAL);
        }

        if(log.isDebugEnabled()){
            log.debug("服务{}，已被注册",service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布服务
     * @param services 封装的是需要发布的服务集合
     * @return this
     */
    public ElephantRPCBootstrap publish(List<ServiceConfig> services) {
        return this;
    }


    /**
     * 启动 Netty 服务
     */
    public void start() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * ------------------------------------核心 API-----------------------------------
     */

    /**
     * 配置引用
     * @param reference 引用配置
     * @return this
     */
    public ElephantRPCBootstrap reference(ReferenceConfig<?> reference) {
        //在这方法里，是否可以拿到相关的配置项【包括配置中心】
        //配置 reference，将来调用 get 方法时，方便生成代理对象
        return this;
    }

}
