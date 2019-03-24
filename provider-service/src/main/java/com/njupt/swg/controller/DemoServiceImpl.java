package com.njupt.swg.controller;

import com.alibaba.dubbo.config.annotation.Service;
import com.njupt.swg.DemoService;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Author swg.
 * @Date 2019/3/24 11:23
 * @CONTACT 317758022@qq.com
 * @DESC
 */
//dubbo的注解，表示暴露服务
@Service
//如果这个组件还要被作为bean使用，则开源用@Component来让spring扫描到，再写个@Service会比较模糊
public class DemoServiceImpl implements DemoService {

    @Value("${dubbo.application.name}")
    private String serviceName;

    public String sayHello(String name) {
        return String.format("[%s] : Hello, %s", serviceName, name);
    }
}
