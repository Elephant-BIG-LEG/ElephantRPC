package com.elephant;

import com.elephant.discovery.Registry;
import com.elephant.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/16:19
 * @Description: 服务调用端核心配置类
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private Registry registry;
    // 分组信息
    private String group;



    /**
     * 代理设计模式 生成一个 API 的代理对象
     * @return 代理对象
     */
    public T get(){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        //动态代理
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //从注册中心中寻找一个可用服务
                //TODO 后期可以修改为自动推荐一个服务 -- 负载均衡
                //TODO 每次调用都去注册中心拉取服务 效率低下 -- 本地缓存 + watcher 机制
                List<InetSocketAddress> addresses = registry.lookup(interfaceRef.getName(),group);
                InetSocketAddress FirstAddress  = addresses.get(0);
                log.info("选择了该服务：【{}】的【{}】节点",interfaceRef.getName(),FirstAddress);
                //2.使用 Netty 发起 RPC 调用


                return null;
            }
        });
        return (T)helloProxy;

    }


    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
