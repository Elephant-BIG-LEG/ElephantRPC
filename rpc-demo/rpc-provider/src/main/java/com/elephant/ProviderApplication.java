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
        ServiceConfig<HelloYrpc> service = new ServiceConfig<>();
        service.setInterface(HelloYrpc.class);
        service.setRef(new HelloYrpcImpl());

        YrpcBootstrap.getInstance()
                .application("first-rpc-provider")
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                //.serialize("jdk")
                // 发布服务
                .publish(null)
                // 扫包批量发布
                //.scan("com.ydlclass")
                // 启动服务
                .start();
    }
}
