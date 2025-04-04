package com.elephant;

import com.elephant.annotation.YrpcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * TODO
 */
@RestController
public class HelloController {
    
    // 需要注入一个代理对象
    @YrpcService
    private HelloYrpc helloYrpc;
    
    @GetMapping("hello")
    public String hello(){
        return helloYrpc.sayHi("provider");
    }
    
}
