package com.elephant;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/22:53
 * @Description: TODO
 */
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }


    /**
     * 动态代理
     * @return
     */
    public T get() {
        //一定是使用动态代理 获取代理对象
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("使用动态代理 hello proxy");
                return null;
            }
        });
        return (T)helloProxy;
    }
}
