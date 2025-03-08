package com.elephant;

import com.elephant.discovery.Registry;
import com.elephant.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

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

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //当使用这个代理对象调用方法时，就会走到这个 invoke 方法
                //1.发现服务，从注册中心，寻找一个可用的服务
                //传入服务名称，返回连接 URL【IP + Port】
                InetSocketAddress address = registry.searchService(interfaceRef.getName());
                if(log.isDebugEnabled()){
                    log.debug("**** The service consumer has discovered the available host of the service【{}】.",
                            interfaceRef.getName(),address);
                }
                //2.使用 netty 连接服务，发送调用的服务的名字 + 方法名字 + 参数列表，得到结果

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

}
