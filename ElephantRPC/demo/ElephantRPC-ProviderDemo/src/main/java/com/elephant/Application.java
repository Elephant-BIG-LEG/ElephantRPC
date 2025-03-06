package com.elephant;

import com.elephant.impl.HelloRPCImpl;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/17:13
 * @Description: TODO
 */
public class Application {
    public static void main(String[] args) {
        //服务提供方，需要注册服务，启动服务
        //1.封装要发布的服务
        ServiceConfig<HelloRPC> service = new ServiceConfig<>();
        service.setInterface(HelloRPC.class);
        service.setRef(new HelloRPCImpl());
        //2.定义注册中心

        //3.通过启动引导程序，启动服务提供方
        //  （1）配置 -- 应用名称 -- 注册中心 -- 序列化协议 -- 压缩方式
        //  （2）发布服务
        ElephantRPCBootstrap.getInstance()
                //配置名称
                .application("first-elephantRPC-provider")
                //配置注册中心
                .register(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                //协议
                .protocol(new ProtocolConfig("jdk"))
                //发布服务
                .publish(service)
                //启动
                .start();


    }

}
