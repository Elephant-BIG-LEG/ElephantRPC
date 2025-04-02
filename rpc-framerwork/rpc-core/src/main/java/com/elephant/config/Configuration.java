package com.elephant.config;

import com.elephant.IdGenerator;
import com.elephant.discovery.RegistryConfig;
import com.elephant.loadbalancer.LoadBalancer;
import com.elephant.loadbalancer.impl.RoundRobinLoadBalancer;
import com.elephant.protection.CircuitBreaker;
import com.elephant.protection.RateLimiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/27/13:54
 * @Description: 服务提供方的核心配置类
 * 相关的基础配置。无论是通过 SPI 还是 XML 我们最重要的就是拿到这个 Type。
 *                对于压缩和序列化实例，我们有两种方式生成
 *                      1.在静态代码块中，压缩工厂和序列化工厂都配置一个默认实例
 *                      2.在 SPI 机制 和 XML 扫描时都会生成对应的实例类，并保存到各自工厂的 Map中
 * 如果 spi 已经加载了，那 xml 会覆盖吗？？？  覆盖取值
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

    public static final String connectString = "zookeeper://127.0.0.1:2181";

    // 配置信息-->注册中心
    private RegistryConfig registryConfig = new RegistryConfig(connectString);

    // 配置信息-->序列化协议
    private String serializeType = "jdk";
    // 这样写没有意义 还是通过统一加载 spi 机制才有作用
    // private SerializerFactory serializerFactory = new SerializerFactory();

    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";
    // 这样写没有意义 还是通过统一加载 spi 机制才有作用
    // private CompressorFactory compressorFactory = new CompressorFactory();

    // 配置信息-->id发射器 -- 1号机房 2号机器
    public IdGenerator idGenerator = new IdGenerator(1, 2);

    // 配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    // 为每一个ip配置一个限流器
    private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
    // 为每一个ip配置一个断路器，熔断
    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);


    public Configuration() {
        // 1、成员变量的默认配置项
        log.info("代码配置 -- 如静态资源、代码配置");

        // 2、spi机制发现相关配置项
        log.info("SPI配置");
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        // 3、使用原生的 API 读取 xml 获得上边的信息
        log.info("xml配置");
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);

        // 4、编程配置项，yrpcBootstrap 提供
        log.info("默认项 -- 具体配置在 YrpcBootstrap 体现");
    }


    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        System.out.println(configuration.toString());
    }
}
