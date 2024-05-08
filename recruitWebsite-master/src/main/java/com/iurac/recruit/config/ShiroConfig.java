package com.iurac.recruit.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.iurac.recruit.security.CustomerRealm;
import com.iurac.recruit.security.RedisCacheManager;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class  ShiroConfig {

    // ShiroDialect 用于将 Shiro 集成到 Thymeleaf 模板中，允许在 HTML 模板中使用 Shiro 特定的标签
    @Bean
    public ShiroDialect shiroDialect(){
        return new ShiroDialect();
    }

    @Bean
    public DefaultWebSecurityManager securityManager(CustomerRealm customerRealm,RedisCacheManager redisCacheManager){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        //新建密码匹配器
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        //设置加密算法：md5
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        //设置加密次数
        hashedCredentialsMatcher.setHashIterations(1024);
        //给自定义域设置该匹配器
        customerRealm.setCredentialsMatcher(hashedCredentialsMatcher);
        //自定义域开启缓存管理
        customerRealm.setCachingEnabled(true);
        customerRealm.setAuthorizationCachingEnabled(true);
        customerRealm.setAuthenticationCachingEnabled(true);
        customerRealm.setAuthorizationCacheName("authorizationCacheName");
        customerRealm.setAuthenticationCacheName("authenticationCacheName");
        customerRealm.setCacheManager(redisCacheManager);
        //安全管理器中设置自定义域
        securityManager.setRealm(customerRealm);
        return securityManager;
    }

    /**
     *
     * ShiroFilterFactoryBean Bean：
     * shiroFilterFactoryBean 配置 Shiro 过滤器链。
     * 它定义了哪些 URL 受保护，应用哪些过滤器。
     * 提供的代码设置了以下内容：
     * securityManager：设置安全管理器。
     * URL 映射：指定 URL 和对应的过滤器。
     * anon 过滤器：表示匿名访问，不需要认证。
     * authc 过滤器：表示需要认证才能访问。
     * */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);//设置安全管理器

        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("/index","anon");
        urlMap.put("/", "anon");
        urlMap.put("/register", "anon");
        urlMap.put("/login", "anon");
        urlMap.put("/logout", "anon");
        urlMap.put("/kaptcha/**", "anon");
        urlMap.put("/layer/**", "anon");
        urlMap.put("/layui/**", "anon");
        urlMap.put("/**/*.js", "anon");
        urlMap.put("/**/*.png", "anon");
        urlMap.put("/druid/**", "anon");

        urlMap.put("/**", "authc");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(urlMap);
        shiroFilterFactoryBean.setLoginUrl("/login");

        return shiroFilterFactoryBean;
    }
}
