package com.iurac.recruit.security;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 这段代码定义了一个名为RedisCacheManager的Spring组件，实现了Shiro的CacheManager接口。
 * 该类的主要功能是为给定的缓存名称创建一个RedisCache实例，从而管理和提供与Redis交互的缓存
 * */
@Component
public class RedisCacheManager implements CacheManager {

    @Autowired
    RedisTemplate redisTemplate;

    //根据给定的缓存名称创建并返回一个新的RedisCache实例。
    //使用cacheName和redisTemplate作为参数来创建RedisCache实例
    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) throws CacheException {
        return new RedisCache<K,V>(cacheName,redisTemplate);
    }
}
