package com.elephant;


import com.elephant.discovery.Registry;
import com.elephant.discovery.RegistryConfig;
import com.elephant.discovery.impl.ZookeeperRegistry;
import com.elephant.utils.Zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
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

    //注册中心
    private Registry registry;

    private int port = 8088;

    //维护一个 zookeeper 实例
//    private ZooKeeper zookeeper;

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
        //尝试使用 registryConfig 获取一个注册中心，有点工厂设计模式的意思了
        this.registry = registryConfig.getRegistry();
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
        //抽象了注册中心的概念, 将服务注册到注册中心 ，这里可以扩展不同的实现
        registry.registry(service);
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
            Thread.sleep(15000);
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
