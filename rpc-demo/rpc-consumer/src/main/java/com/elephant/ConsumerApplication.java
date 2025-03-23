package com.elephant;

import com.elephant.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/14:03
 * @Description: TODO
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {

        // 想尽一切办法获取代理对象,使用ReferenceConfig进行封装
        // reference一定用生成代理的模板方法，get()
        ReferenceConfig<HelloYrpc> reference = new ReferenceConfig<>();
        //后面调用 get 方法时，会自动生成相对应的代理对象
        reference.setInterfaceRef(HelloYrpc.class);

        // 代理做了些什么?
        // 1、连接注册中心
        // 2、拉取服务列表
        // 3、选择一个服务并建立连接
        // 4、发送请求，携带一些信息（接口名，参数列表，方法的名字），获得结果
        YrpcBootstrap.getInstance()
                .application("first-yrpc-consumer")
                //配置一个注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .compress("gzip")
                //.group("primary")
                //会将上面的注册中心放到这个核心配置类中
                .reference(reference);

        HelloYrpc helloYrpc = reference.get();
        //这里才会去触发 invoke 方法
        String string = helloYrpc.sayHi("你好");
        log.info("成功收到服务提供方发送的数据:{}",string);


//        while (true) {
////            try {
////                Thread.sleep(10000);
////                System.out.println("++------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
////            } catch (InterruptedException e) {
////                throw new RuntimeException(e);
////            }
//            for (int i = 0; i < 50; i++) {
//                String sayHi = helloYrpc.sayHi("你好yrpc");
//                log.info("sayHi-->{}", sayHi);
//            }
//        }
    }
}
