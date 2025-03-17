package com.elephant;

import com.elephant.discovery.Registry;
import com.elephant.discovery.RegistryConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/16:19
 * @Description: 服务调用端核心配置类
 */
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private Registry registry;
    // 分组信息
    private String group;

    private RegistryConfig registryConfig;


    public T get(){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        //动态代理
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //1.注册服务 --- 这里想要实现注册就需要从核心配置类拉去注册中心过来
                Registry registry = registryConfig.getRegistry(true);
                registry.lookup(proxy.toString(),group);
                //2.使用 Netty 发起 RPC 调用

                return null;
            }
        });
        return (T)helloProxy;

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

    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }
}
