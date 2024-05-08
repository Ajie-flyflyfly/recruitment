package com.iurac.recruit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     *
     * 定义了一个资源处理器，将 URL /pic/** 映射到本地文件系统路径 file:D:/upload/。
     * 这意味着当用户访问 /pic/ 开头的 URL 时，Spring 将从 D:/upload/ 目录中查找相应的静态资源
     * */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/pic/**").addResourceLocations("file:D:/upload/");
    }
}