package com.iurac.recruit.security;

import com.iurac.recruit.entity.User;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 这段代码定义了一个名为RedisCache的类，该类实现了Shiro的Cache接口，用于在Redis中存储和管理缓存数据。
 * 该类使用了泛型来支持任意类型的键（k）和值（v）。
 * */

/**
 * 注意：这个类假设了键可以是任何类型，但如果键是User类型，它将使用User的username作为哈希表的字段名。
 * 这可能会导致问题，如果有两个不同的User对象具有相同的username，那么它们将会在哈希表中映射到同一个值。
 * */
public class RedisCache<k,v> implements Cache<k,v> {


    private String cacheName; //缓存的名称，用于标识在Redis中的哪个哈希表存储数据
    private RedisTemplate redisTemplate; //用于与Redis交互的RedisTemplate对象
    public RedisCache() {
    }

    public RedisCache(String cacheName,RedisTemplate redisTemplate) {
        this.cacheName = cacheName;
        this.redisTemplate = redisTemplate;
    }

    //从Redis中获取键对应的值。
    //如果键是User类型，使用其username作为哈希表的字段名
    @Override
    public v get(k k) throws CacheException {
        String s = k.toString();
        if(k instanceof User){
            s = ((User) k).getUsername();
        }
        System.out.println("get==========="+s+"-->("+cacheName+")");
        return (v) redisTemplate.opsForHash().get(this.cacheName,s);
    }

    //将键值对存储到Redis中。
    //如果键是User类型，使用其username作为哈希表的字段名
    @Override
    public v put(k k, v v) throws CacheException {
        String s = k.toString();
        if(k instanceof User){
            s = ((User) k).getUsername();
        }
        redisTemplate.opsForHash().put(this.cacheName,s,v);
        System.out.println("put==========="+s+"==========="+v.toString()+"-->("+cacheName+")");
        return v;
    }

    //从Redis中删除指定键的值。
    //如果键是User类型，使用其username作为哈希表的字段名。
    @Override
    public v remove(k k) throws CacheException {
        String s = k.toString();
        if(k instanceof User){
            s = ((User) k).getUsername();
        }
        System.out.println("delete==========="+s+"-->("+cacheName+")");
        return (v) redisTemplate.opsForHash().delete(this.cacheName,s);
    }

    //清空指定缓存的所有数据
    @Override
    public void clear() throws CacheException {
        redisTemplate.delete(this.cacheName);
    }

    //返回指定缓存的大小（即哈希表中的键值对数量）
    @Override
    public int size() {
        return redisTemplate.opsForHash().size(this.cacheName).intValue();
    }

    //返回指定缓存的所有键
    @Override
    public Set<k> keys() {
        return redisTemplate.opsForHash().keys(this.cacheName);
    }

    //返回指定缓存的所有值
    @Override
    public Collection<v> values() {
        return redisTemplate.opsForHash().values(this.cacheName);
    }
}
