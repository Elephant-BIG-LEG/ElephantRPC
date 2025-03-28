package com.elephant.config;

import com.elephant.compress.Compressor;
import com.elephant.compress.CompressorFactory;
import com.elephant.loadbalancer.LoadBalancer;
import com.elephant.serialize.Serializer;
import com.elephant.serialize.SerializerFactory;
import com.elephant.spi.SpiHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/28/8:50
 * @Description: TODO
 */
@Slf4j
public class SpiResolver {
    /**
     * 通过 SPI 方式加载配置项
     * @param configuration 配置项
     * @return 返回加载后的 SPI 对象
     */
    public void loadFromSpi(Configuration configuration) {
        log.info("通过 spi 的方式加载配置项");
        // 我的spi的文件中配置了很多实现（自由定义，只能配置一个实现，还是多个）
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        // 将其放入工厂
        if(loadBalancerWrappers != null && loadBalancerWrappers.size() > 0){
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> objectWrappers = SpiHandler.getList(Compressor.class);
        if(objectWrappers != null){
            objectWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerObjectWrappers = SpiHandler.getList(Serializer.class);
        if (serializerObjectWrappers != null){
            serializerObjectWrappers.forEach(SerializerFactory::addSerializer);
        }
    }
}
