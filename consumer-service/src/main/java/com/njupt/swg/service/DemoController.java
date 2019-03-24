package com.njupt.swg.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.swg.DemoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author swg.
 * @Date 2019/3/24 13:03
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@RestController
public class DemoController {
    @Reference
    private DemoService demoService;

    @RequestMapping("test")
    public String test(@PathVariable("name") String name){
        String res = demoService.sayHello(name);
        return res;
    }
}
