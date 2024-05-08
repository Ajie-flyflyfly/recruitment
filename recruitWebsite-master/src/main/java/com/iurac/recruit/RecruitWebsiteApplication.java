package com.iurac.recruit;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
* Spring Boot应用程序的主类。这个类是应用程序的入口点。

@SpringBootApplication：这是一个方便的注解，它添加了以下所有内容：
@Configuration：将类标记为应用程序上下文的bean定义源。
@EnableAutoConfiguration：告诉Spring Boot根据类路径设置、其他bean和各种属性设置开始添加bean。
@ComponentScan：告诉Spring在com.iurac.recruit包中查找其他组件、配置和服务。
*
@MapperScan("com.iurac.recruit.mapper")：这告诉MyBatis在com.iurac.recruit.mapper包中查找映射器并为它们创建实现。
public static void main(String[] args)：这是可以运行以启动应用程序的入口点方法。它通过调用run委托给Spring Boot的SpringApplication类。SpringApplication.run()方法启动了一个应用程序。
* */

@SpringBootApplication
@MapperScan("com.iurac.recruit.mapper")
public class RecruitWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitWebsiteApplication.class, args);
    }

}
