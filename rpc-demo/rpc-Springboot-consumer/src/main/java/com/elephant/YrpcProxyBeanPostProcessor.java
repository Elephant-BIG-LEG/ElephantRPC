package com.elephant;

import com.elephant.annotation.YrpcService;
import com.elephant.proxy.YrpcProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/04/04/11:30
 * @Description: TODO
 */
@Component
public class YrpcProxyBeanPostProcessor implements BeanPostProcessor {

    // 他会拦截所有的bean的创建，会在每一个bean初始化后被调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 想办法给他生成一个代理
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            YrpcService yrpcService = field.getAnnotation(YrpcService.class);
            if(yrpcService != null){
                // 获取一个代理
                Class<?> type = field.getType();
                Object proxy = YrpcProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}