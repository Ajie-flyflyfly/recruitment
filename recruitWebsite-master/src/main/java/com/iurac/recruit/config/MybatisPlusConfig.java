package com.iurac.recruit.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 用于配置MyBatis Plus的拦截器。
 * public MybatisPlusInterceptor mybatisPlusInterceptor(): 这个方法定义了一个MybatisPlusInterceptor的bean。
 * MybatisPlusInterceptor是MyBatis Plus的拦截器，可以添加各种内部拦截器来增强其功能。
 * interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)):
 * 这行代码添加了一个PaginationInnerInterceptor到MybatisPlusInterceptor中。
 * PaginationInnerInterceptor是MyBatis Plus的一个内部拦截器，用于实现分页功能。
 * DbType.MYSQL表示数据库类型是MySQL。
 * */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}
