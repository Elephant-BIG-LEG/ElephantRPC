package com.elephant;


import lombok.extern.slf4j.Slf4j;

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
        return this;
    }

    /**
     * 用来注册一个注册中心
     * @param registryConfig 注册中心
     * @return this 当前实例
     */
    public ElephantRPCBootstrap register(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return this
     */
    public ElephantRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        if (log.isDebugEnabled()){
            log.debug("当前工程使用了:{} 协议",protocolConfig);
        }
        return null;
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
