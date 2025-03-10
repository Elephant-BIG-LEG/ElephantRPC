package com.elephant;

import com.elephant.discovery.Registry;
import com.elephant.proxy.handler.RpcConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/22:53
 * @Description: TODO
 */
@Slf4j
public class ReferenceConfig<T> {
    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    private Class<T> interfaceRef;

    private Registry registry;

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    /**
     * 代理设计模式，生成一个 API 接口的代理对象
     * @return
     */
    public T get() {
        //一定是使用动态代理 获取代理对象
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        InvocationHandler handler = new RpcConsumerInvocationHandler(registry,interfaceRef);
        //封装了 invoke 方法
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T)helloProxy;
    }

    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }
}
