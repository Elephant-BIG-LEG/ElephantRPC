package com.elephant;

import com.elephant.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/17:12
 * @Description: TODO
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        //想尽一切方法获取代理对象，使用 ReferenceConfig 进行封装
        //reference 中一定有用于生成代理的模板方法 get()
        //第一个接口
        ReferenceConfig<HelloRPC> reference = new ReferenceConfig();
        reference.setInterface(HelloRPC.class);

        //代理做了什么？
        //1.连接注册中心
        //2.拉去服务列表
        //3.选择一个服务进行连接
        //4.发送请求，携带请求参数（接口名，参数列表，方法的名字），获得结果
        ElephantRPCBootstrap.getInstance()
                .application("first-elephantRPC-consumer")
                .register(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        //使用代理对象进行远程调用
        HelloRPC helloRPC = reference.get();
        String sayHi = helloRPC.sayHi("你好");
        log.info("Receiving the msg:{}",sayHi);
    }
}
