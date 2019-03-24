package com.njupt.swg.service;

import com.njupt.swg.DemoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Author swg.
 * @Date 2019/3/24 17:46
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Component
public class StubService implements DemoService {
    private final DemoService demoService;
    public StubService(DemoService demoService){
        this.demoService = demoService;
    }

    @Override
    public String sayHello(String name) {
        System.out.println("本地存根开始执行...");
        if(name.equalsIgnoreCase("fossi")) {
            System.out.println("你就是fossi啊，对对对");
            return demoService.sayHello(name);
        }
        System.out.println("你他娘不是fossi!");
        return "你搞错了";
    }
}
