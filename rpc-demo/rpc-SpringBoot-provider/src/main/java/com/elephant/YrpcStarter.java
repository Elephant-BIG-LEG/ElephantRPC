package com.elephant;

import com.elephant.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/04/04/11:31
 * @Description: TODO
 */
@Component
@Slf4j
public class YrpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.info("yrpc 开始启动...");
        YrpcBootstrap.getInstance()
                .application("first-rpc-provider")
                // 以下的配置都会覆盖 Configuration 的配置
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .compress("gzip")
                // 发布服务
                //.publish(service)
                // 扫包批量发布
                .scan("com.elephant.impl")
                // 启动 Netty 服务
                .start();

    }
}
