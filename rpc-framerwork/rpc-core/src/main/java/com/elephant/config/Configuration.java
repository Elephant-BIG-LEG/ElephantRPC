package com.elephant.config;

import com.elephant.IdGenerator;
import com.elephant.discovery.RegistryConfig;
import com.elephant.loadbalancer.LoadBalancer;
import com.elephant.loadbalancer.impl.RoundRobinLoadBalancer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

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

    public Configuration() {
        // 1、成员变量的默认配置项
        log.info("代码配置");

        // 2、spi机制发现相关配置项
        log.info("SPI配置");
//        SpiResolver spiResolver = new SpiResolver();
//        spiResolver.loadFromSpi(this);

        // 3、读取xml获得上边的信息
        log.info("xml配置");
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);

        // 4、编程配置项，yrpcBootstrap提供
        log.info("默认项");
    }



    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }
}
