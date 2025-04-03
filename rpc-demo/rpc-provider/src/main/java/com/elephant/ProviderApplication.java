package com.elephant;

import com.elephant.discovery.RegistryConfig;
import com.elephant.impl.HelloYrpcImpl;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/14:03
 * @Description: TODO
 */
public class ProviderApplication {
    public static void main(String[] args) {
//        ServiceConfig<HelloYrpc> service = new ServiceConfig<>();
//        service.setInterface(HelloYrpc.class);
//        //服务的具体实现对象
//        service.setRef(new HelloYrpcImpl());

        /**
         * TODO 启动的两种方式
         *  1.使用 Configuration 的默认配置
         *  2.手动传参覆盖 Configuration 的默认配置
         */
        YrpcBootstrap.getInstance()
                .application("first-rpc-provider")
                // 以下的配置都会覆盖 Configuration 的配置
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                //.compress("gzip")
                // 发布服务
                //.publish(service)
                // 扫包批量发布
                .scan("com.elephant")
                // 启动 Netty 服务
                .start();

    }
}
