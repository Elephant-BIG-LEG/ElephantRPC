package com.elephant;


import com.elephant.annotation.ElephantAPI;
import com.elephant.channelHandler.handler.MethodCallHandler;
import com.elephant.channelHandler.handler.YrpcRequestDecoder;
import com.elephant.channelHandler.handler.YrpcResponseEncoder;
import com.elephant.config.Configuration;
import com.elephant.core.HeartbeatDetector;
import com.elephant.discovery.RegistryConfig;
import com.elephant.protection.CircuitBreaker;
import com.elephant.protection.RateLimiter;
import com.elephant.transport.message.YrpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/14:41
 * @Description: 启动核心类
 */
@Slf4j
public class YrpcBootstrap<T> {

    /**
     * --------------------------- 相关的基础配置 --------------------------------
     */

    // YrpcBootstrap是个单例，我们希望每个应用程序只有一个实例
    private static final YrpcBootstrap yrpcBootstrap = new YrpcBootstrap();

    public final Configuration configuration;

    //注意：如果使用 InetSocketAddress 作为 key，一定要保证该类重写了 toString 方法和 equals 方法
    //每一个地址维护一个 channel
    public static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    //有序的保存channel的响应时间
    public static TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    //当服务调用方，通过接口、方法名、具体的方法参数列表发起调用，提供怎么知道使用哪一个实现
    // (1) new 一个  （2）spring beanFactory.getBean(Class)  (3) 自己维护映射关系
    // 维护已经发布且暴露的服务列表 key-> interface的全限定名  value -> ServiceConfig
    public final static Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);

    //定义全局的对外挂起的 completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    // 保存request对象，可以到当前线程中随时获取
    public static final ThreadLocal<YrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * --------------------------- 服务提供方相关 API --------------------------------
     */

    private YrpcBootstrap() {
        configuration = new Configuration();
        if(log.isDebugEnabled()){
            log.debug("已经通过 Configuration 加载配置信息");
        }
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static YrpcBootstrap getInstance() {
        return yrpcBootstrap;
    }


    public YrpcBootstrap application(String appName) {
        log.info("为该服务起一个名字：{}", appName);
        configuration.setAppName(appName);
        return this;
    }


    /**
     * 设置注册中心
     *
     * @param registryConfig
     * @return
     */
    public YrpcBootstrap registry(RegistryConfig registryConfig) {
        // TODO 这里好像没必要传参了，因为 Configuration 都会配置，除非想手动修改
        log.info("开始注册该服务：{}", registryConfig);
        //使用模板方法
        //true 表示使用默认配置
        configuration.setRegistryConfig(registryConfig);
        return this;

    }

//    /**
//     * TODO 没必要？？？
//     * 配置负载均衡器
//     * @param loadBalancer
//     * @return
//     */
//    public YrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
//        configuration.setLoadBalancer(loadBalancer);
//        return this;
//    }

    /**
     * 发布服务提供方的相关节点
     *
     * @param service
     * @return
     */
    public YrpcBootstrap publish(ServiceConfig<?> service) {
        //抽象注册中心的概念
        configuration.getRegistryConfig().getRegistry(true).register(service);

        SERVERS_LIST.put(service.getInterface().getName(), service);
        return this;
    }


    /**
     * 启动 Netty 服务
     */
    public void start() {
        if (log.isDebugEnabled()) {
            log.debug("服务提供方启用");
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workGroup = new NioEventLoopGroup(10);

        try {
            //引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            //TODO 服务提供方处理数据
//                            new SimpleChannelInboundHandler<>() {
//                                @Override
//                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
//                                    ByteBuf byteBuf = (ByteBuf) msg;
//                                    log.info("byteBuf:{}" ,byteBuf.toString(Charset.defaultCharset()));
//                                    log.info("服务提供方开始写回数据");
//                                    channelHandlerContext.channel().writeAndFlush(
//                                            Unpooled.copiedBuffer("这是写回的信息".getBytes(StandardCharsets.UTF_8)));
//                                }
//                            }

                            socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
                                    // 解码
                                    .addLast(new YrpcRequestDecoder())
                                    // 根据请求进行方法调用 -- 最耗时
                                    .addLast(new MethodCallHandler())
                                    // 编码
                                    .addLast(new YrpcResponseEncoder());
                        }
                    });
            //绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                log.info("下线服务");
                bossGroup.shutdownGracefully().sync();
                workGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 扫包加载 -- 加载要暴露的接口
     *
     * @param packageName 要进行扫描的包名称
     * @return
     */
    public YrpcBootstrap scan(String packageName) {
        log.info("开始扫包批量发布，packageName获取绝对路径");
        // 1、需要通过packageName获取其下的所有的类的权限定名称
        List<String> classNames = getAllClassNames(packageName);
        // 2、通过反射获取他的接口，构建具体实现
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(ElephantAPI.class) != null)
                .collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            // 获取他的接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            // TODO 分组信息

            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                if (log.isDebugEnabled()){
                    log.debug("---->已经通过包扫描，将服务【{}】发布.",anInterface);
                }
                // 3、发布
                publish(serviceConfig);
            }

        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        // 1、通过packageName获得绝对路径
        // com.ydlclass.xxx.yyy -> E://xxx/xww/sss/com/ydlclass/xxx/yyy
        String basePath = packageName.replaceAll("\\.","/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(url == null){
            throw new RuntimeException("包扫描时，发现路径不存在.");
        }
        String absolutePath = url.getPath();
        //
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath,classNames,basePath);
        return classNames;
    }

    /**
     * 递归处理文件
     * @param absolutePath
     * @param classNames
     * @param basePath
     * @return
     */
    private List<String> recursionFile(String absolutePath, List<String> classNames,String basePath) {
        // 获取文件
        File file = new File(absolutePath);
        // 判断文件是否是文件夹
        if (file.isDirectory()){
            // 找到文件夹的所有的文件
            File[] children = file.listFiles(pathname -> pathname.isDirectory()
                    || pathname.getPath().contains(".class"));
            if(children == null || children.length == 0){
                return classNames;
            }
            for (File child : children) {
                if(child.isDirectory()){
                    // 递归调用
                    recursionFile(child.getAbsolutePath(),classNames,basePath);
                } else {
                    // 文件 --> 类的权限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(),basePath);
                    classNames.add(className);
                }
            }
        } else {
            // 文件 --> 类的权限定名称
            String className = getClassNameByAbsolutePath(absolutePath,basePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath,String basePath) {
        // E:\project\ydlclass-yrpc\yrpc-framework\yrpc-core\target\classes\com\ydlclass\serialize\Serializer.class
        // com\ydlclass\serialize\Serializer.class --> com.ydlclass.serialize.Serializer
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        return fileName;
    }

    /**
     * --------------------------- 服务调用端相关 API --------------------------------
     */

    public YrpcBootstrap reference(ReferenceConfig<?> reference) {
        if (log.isDebugEnabled()) {
            log.debug("心跳检测器开始工作");
        }
        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName(), null);
        log.info("通过核心配置类去完善服务调用端的配置类");
        //将注册中心的实例设置到 reference 中
        reference.setRegistry(configuration.getRegistryConfig().getRegistry(true));
        return this;
    }


    public YrpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()) {
            log.debug("使用：{}进行序列化", serializeType);
        }
        return this;
    }

    public YrpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()) {
            log.debug("使用：{}进行压缩", compressType);
        }
        return this;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
