package com.elephant;

import com.elephant.discovery.Registry;
import com.elephant.proxy.handler.RpcConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

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
        Class<T>[] classes = new Class[]{interfaceRef};

        //获取 handler
        InvocationHandler handler = new RpcConsumerInvocationHandler(interfaceRef,registry,group);

        //动态代理
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);

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
