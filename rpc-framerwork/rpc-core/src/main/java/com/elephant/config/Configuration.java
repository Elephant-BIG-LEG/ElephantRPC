package com.elephant.config;

import com.elephant.IdGenerator;
import com.elephant.discovery.RegistryConfig;
import com.elephant.loadbalancer.LoadBalancer;
import com.elephant.loadbalancer.impl.RoundRobinLoadBalancer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/27/13:54
 * @Description: 相关的基础配置
 */
@Data
@Slf4j
public class Configuration {

    // 配置信息-->端口号
    private int port = 8094;

    // 配置信息-->应用程序的名字
    private String appName = "default";

    // 分组信息
    private String group = "default";

    // 配置信息-->注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    // 配置信息-->序列化协议
    private String serializeType = "jdk";

    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";

    // 配置信息-->id发射器 -- 1号机房 2号机器
    public IdGenerator idGenerator = new IdGenerator(1, 2);

    // 配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();
}
