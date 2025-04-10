package com.elephant.compress;


import com.elephant.compress.impl.GzipCompressor;
import com.elephant.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 */
@Slf4j
public class CompressorFactory {
    /**
     * key：压缩名称
     * value：压缩实例
     */
    private final static Map<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    /**
     * key：压缩方式序号
     * value：压缩实例
     */
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);

    // 默认的压缩方式
    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);
    }
    
    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressorType 序列化的类型
     * @return CompressWrapper
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE.get(compressorType);
        if(compressorObjectWrapper == null){
            log.error("未找到您配置的【{}】压缩算法，默认选用gzip算法。",compressorType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }
    
    public static ObjectWrapper<Compressor> getCompressor(byte compressorCode) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        if ((compressorObjectWrapper == null)){
            log.error("未找到您配置的编号为【{}】的压缩算法，默认选用gzip算法。",compressorCode);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }
    
    /**
     * 给工厂中新增一个压缩方式
     * @param compressorObjectWrapper 压缩类型的包装
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper){
        COMPRESSOR_CACHE.put(compressorObjectWrapper.getName(),compressorObjectWrapper);
        COMPRESSOR_CACHE_CODE.put(compressorObjectWrapper.getCode(),compressorObjectWrapper);
    }
}
