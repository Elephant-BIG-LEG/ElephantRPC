package com.elephant.spi;

import com.elephant.config.Configuration;
import com.elephant.config.ObjectWrapper;
import com.elephant.exception.SpiException;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/28/8:47
 * @Description: TODO
 */
@Slf4j
public class SpiHandler {

    // 定义一个 basePath
    private static final String BASE_PATH = "META-INF/yrpc-services";

    // 缓存，保存 spi 相关的原始内容 key：文件绝对地址 value：文件具体内容
    private static Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);

    // 缓存的是每一个接口锁对应的实现的实例
    private static final Map<Class<?>,List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);

    // 加载当前类之后需要将 spi 信息进行保存，避免运行时执行 io 操作
    static {
        // 加载当前 工程 和 jar 包中的 classPath 中的资源
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        File file = new File(fileUrl.getPath());
        if (file != null) {
            File[] children = file.listFiles();
            for (File child : children) {
                String key = child.getName();
                List<String> value = getImplNames(child);
                SPI_CONTENT.put(key, value);
            }
        }
    }

    /**
     * 获取文件所有的实现名称
     *
     * @param child 文件绝对地址
     * @return 实现类的权限定名称结合
     */
    private static List<String> getImplNames(File child) {

        try {
            // 装饰器模式
            FileReader fileReader = new FileReader(child);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            List<String> implNames = new ArrayList<>();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)) {
                    break;
                }
                implNames.add(line);
            }
            return implNames;
        } catch (IOException e) {
            log.error("spi 读取异常，使用后面的兜底策略");
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取所有和当前服务相关的实例
     * @param clazz 一个服务接口的class实例
     * @return       实现类的实例集合
     * @param <T>
     */
    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz) {

        // 1、优先走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return objectWrappers.stream().map( wrapper -> (ObjectWrapper<T>)wrapper )
                    .collect(Collectors.toList());
        }

        // 2、构建缓存
        buildCache(clazz);

        // 3、再次获取
        objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return objectWrappers.stream().map( wrapper -> (ObjectWrapper<T>)wrapper )
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 获取第一个和当前服务相关的实例
     * @param clazz 一个服务接口的class实例
     * @return      实现类的实例
     * @param <T>
     */
    public synchronized static <T> ObjectWrapper<T> get(Class<T> clazz) {

        // 1、优先走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return (ObjectWrapper<T>)objectWrappers.get(0);
        }

        // 2、构建缓存
        buildCache(clazz);

        // 这里还是从缓存去取，有一次保证了构建缓存的正确性
        List<ObjectWrapper<?>>  result = SPI_IMPLEMENT.get(clazz);
        if (result == null || result.size() == 0){
            return null;
        }

        // 3、再次尝试获取第一个
        return (ObjectWrapper<T>)result.get(0);
    }

    /**
     * 构建clazz相关的缓存
     * @param clazz 一个类的class实例
     */
    private static void buildCache(Class<?> clazz) {

        log.info("构建 clazz 相关的缓存");

        // 1、通过clazz获取与之匹配的实现名称
        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);
        if(implNames == null || implNames.size() == 0){
            return;
        }

        // 2、实例化所有的实现
        List<ObjectWrapper<?>> impls = new ArrayList<>();
        for (String implName : implNames) {
            try {
                // 首先进行分割
                String[] codeAndTypeAndName = implName.split("-");
                if(codeAndTypeAndName.length != 3){
                    throw new SpiException("您配置的spi文件不合法");
                }
                Byte code = Byte.valueOf(codeAndTypeAndName[0]);
                String type = codeAndTypeAndName[1];
                String implementName = codeAndTypeAndName[2];

                // 建一个实例
                Class<?> aClass = Class.forName(implementName);
                Object impl = aClass.getConstructor().newInstance();

                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code,type,impl);

                impls.add(objectWrapper);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e){
                log.error("实例化【{}】的实现时发生了异常",implName,e);
            }

        }
        SPI_IMPLEMENT.put(clazz,impls);
    }


    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }
}
