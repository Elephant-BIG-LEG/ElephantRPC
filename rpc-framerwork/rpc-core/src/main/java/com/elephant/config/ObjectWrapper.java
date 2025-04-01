package com.elephant.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/22/11:44
 * @Description: 统一包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectWrapper<T> {
    // 服务代码
    private Byte code;
    // 实例名称
    private String name;
    // 具体接口实例
    private T impl;
}

