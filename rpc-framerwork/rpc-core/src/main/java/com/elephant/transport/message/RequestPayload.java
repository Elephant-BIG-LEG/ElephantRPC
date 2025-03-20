package com.elephant.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/20/20:15
 * @Description: 请求调用方请求接口方法的描述
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {

    // 1、接口的名字 -- com.ydlclass.HelloYrpc
    private String interfaceName;

    // 2、方法的名字 --sayHi
    private String methodName;

    // 3、参数列表，参数分为参数类型和具体的参数
    // 参数类型用来确定重载方法，具体的参数用来执行方法调用
    private Class<?>[] parametersType;  // -- {java.long.String}
    private Object[] parametersValue;   // -- 参数值

    // 4、返回值的封装 -- {java.long.String}
    private Class<?> returnType;

}
