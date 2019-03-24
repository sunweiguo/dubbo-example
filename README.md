# dubbo-example
Spring Boot整合2.6.5版本dubbo，并且将监控信息发送到监测台


## 一、传统xml方式

一个服务者，一个消费者，服务者将服务注册到`Registry`，这个用`zookeeper`来实现。

服务者：服务层，即`taotao-manager-service`的`applicationContext-service.xml`将接口注册到`zookeeper`上。


```xml
<!-- 发布dubbo服务 -->
<!-- 提供方应用信息，用于计算依赖关系 -->
<dubbo:application name="taotao-manager" />
<!-- 注册中心的地址 -->
<dubbo:registry protocol="zookeeper" address="ip address:2181" />
<!-- 用dubbo协议在20880端口暴露服务 -->
<dubbo:protocol name="dubbo" port="20880" />
<!-- 声明需要暴露的服务接口 -->
<dubbo:service interface="com.njupt.swg.service.ItemService" ref="itemServiceImpl" timeout="300000"/>
```

消费者：表现层，即`taotao-manager-web的apringmvc.xml`中订阅到注册上去的服务。


```xml
<!-- 引用dubbo服务 -->
<dubbo:application name="taotao-manager-web"/>
<dubbo:registry protocol="zookeeper" address="ip地址:2181"/>	
<dubbo:reference interface="com.njupt.swg.service.ItemService" id="itemService" />
```

主要就是以上几个配置，截取于淘淘商城某笔记。依赖就是dubbo和zookeeper客户端。这个就不做了，下面直接上springboot.具体可参考官方文档：[快速启动](https://dubbo.incubator.apache.org/zh-cn/docs/user/quick-start.html)以及[schemal含义](https://dubbo.incubator.apache.org/zh-cn/docs/user/references/xml/dubbo-service.html)

## 二、springboot2.x+dubbo2.6.5实战

本节的代码为：[dubbo-example](https://github.com/sunweiguo/dubbo-example)

##### 1、创建聚合工程

创建一个父亲项目，管理pom，所有的`modules`都受其管理。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.njupt.swg</groupId>
    <artifactId>dubbo-example</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>dubbo-example</name>
    <packaging>pom</packaging>
    <description>Demo project for Spring Boot</description>

    <modules>
        <module>provider-service</module>
        <module>consumer-service</module>
        <module>dubbo-api</module>
    </modules>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
        <curator-framework.version>4.0.1</curator-framework.version>
        <zookeeper.version>3.4.13</zookeeper.version>
        <dubbo.starter.version>0.2.1.RELEASE</dubbo.starter.version>
        <dubbo.version>2.6.5</dubbo.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!--dubbo-springboot-->
        <dependency>
            <groupId>com.alibaba.boot</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
            <version>${dubbo.starter.version}</version>
        </dependency>

        <!--dubbo-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dubbo</artifactId>
            <version>${dubbo.version}</version>
        </dependency>

        <!--curator相关，操作zookeeper-->
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-framework</artifactId>
            <version>${curator-framework.version}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>${curator-framework.version}</version>
        </dependency>

        <!--zookeeper-->
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>${zookeeper.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>


</project>
```



##### 2、公共的API接口

新创建一个`module`叫做`dubbo-api`，里面放的是公共的调用接口：


```java
public interface DemoService {
    String sayHello(String name);
}
```

##### 3、服务提供者

首先就是pom文件，处理继承父类意外，额外需要注意的是需要依赖于上面一个公共的接口工程，由于API工程是以jar形式存在，所以跟引用普通jar文件一样去依赖它：


```xml
<dependencies>
    <dependency>
        <groupId>com.njupt.swg</groupId>
        <artifactId>dubbo-api</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

这样，服务提供者就可以用这个接口了，后面的消费者也一样，就可以拿到这个接口去消费了。这里先完成服务提供者的实现类：


```java
import com.alibaba.dubbo.config.annotation.Service;
import com.njupt.swg.DemoService;
import org.springframework.beans.factory.annotation.Value;

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
```

注意这里的注解`@Service`不是`spring`中的包注解，而是`dubbo`中用于暴露服务出去的注解，所以千万不要搞错。需要注意的是，如果还是需要将其作为`spring`的一个`bean`的话，则可以用`@Component`来注解，避免混淆。

另外最需要注意的就是配置文件了：


```properties
# Spring boot 应用名称
spring.application.name=provider-service
server.port=9001

# Dubbo服务名称
## The default value of dubbo.application.name is ${spring.application.name}
dubbo.application.name=provider-service

# 通信使用dubbo协议，端口为20880
dubbo.protocol.name=dubbo
dubbo.protocol.port=20880

# 以zookeeper为注册中心
dubbo.registry.address=127.0.0.1:2181
dubbo.registry.protocol=zookeeper

# monitor监控台
dubbo.monitor.protocol=registry
```

这些其实跟传统的xml配置方式是一样的，所以大体都是一样的。最后在启动函数上添加：


```java
@EnableDubbo
```
这样启动服务提供者，不报错的话，那么在服务管理中心就可以查看到这个服务已经起来了，加上我们已经准备好的`monitor`，此时应该有两个服务在上面了。


##### 4、服务消费者

关于配置文件、pom文件以及启动函数上的注解都跟上面类似，不再赘述。下面直接尝试去调用服务：

```java
import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.swg.DemoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    @Reference
    private DemoService demoService;

    @RequestMapping("test")
    public String test(){
        String res = demoService.sayHello("fossi!");
        return res;
    }
}
```

注意这里的`@Reference`不要引用错了。启动，此时不出意外的话，应该在服务治理页面上会看到如下：

![image](http://bloghello.oursnail.cn/dubbo3-1.jpg)

我们来消费一下：

![image](http://bloghello.oursnail.cn/dubbo3-2.jpg)

监控台也可以接受到消费的信息：

![image](http://bloghello.oursnail.cn/dubbo3-3.jpg)

这样，一次完整的服务提供和消费的实战就完成了。
