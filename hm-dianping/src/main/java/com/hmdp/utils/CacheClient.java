package com.hmdp.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.hmdp.entity.Shop;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.RedisConstants.LOCK_SHOP_KEY;

/**
 * @Author W-ch
 * @Time 2023/1/13 13:54
 * @E-mail wang.xiaohong.0817@gmail.com
 * @File CacheClient .java
 * @Software IntelliJ IDEA
 */
@Slf4j
@Component
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;
    
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    
    public CacheClient(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    /**
     * 缓存穿透
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void set(String key, Object value, Long time, TimeUnit unit){
        String jsonStr = JSONUtil.toJsonStr(value);
    
        stringRedisTemplate.opsForValue().set(key,jsonStr, time, unit);
    }
    
    /**
     * 逻辑过期解决缓存击穿
     * @param key
     * @param value
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        RedisData redisData = new RedisData();
        
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        
        String jsonStr = JSONUtil.toJsonStr(redisData);
    
        stringRedisTemplate.opsForValue().set(key,jsonStr);
    }
    
    /**
     * 解决缓存穿透
     * @param keyPrefix
     * @param id
     * @param type
     * @param time
     * @param unit
     * @param dbFallback
     * @return
     * @param <R>
     * @param <ID>
     */
    public <R,ID> R queryWithPassThrough
            (String keyPrefix,ID id,
             Class<R> type,
             Long time, TimeUnit unit,
             Function<ID,R> dbFallback
            ){
        String key = keyPrefix + id;
        
        // 从缓存获取店铺信息
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(jsonStr)) {
            return JSONUtil.toBean(jsonStr,type);
        }
        
        // 判断是否为空值
        if (Objects.equals(jsonStr,"")){
            return null;
        }
        
        // 不存在，根据数据库查询
        R r = dbFallback.apply(id);
        
        // 不能存在，向缓存中存入空值
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key,"",time,unit);
            return null;
        }
        
        this.set(key, r,time, unit);
        
        return r;
    }
    
    
    public <R,ID> R queryWhitLogicalExpire(String keyPrefix,ID id,Class<R> type,Function<ID,R> dbFallback,Long time, TimeUnit unit){
        String key = CACHE_SHOP_KEY + id;
        
        // 1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        
        // 2.判断是否存在
        if (StrUtil.isBlank(shopJson)) {
            
            // 3 不存在，则返回
            return null;
        }
        //4 命中，反序列化为RedisData
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(data, type);
        
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 未过期，返回店铺信息
            return r;
        }
        // 5.2 过期，进行缓存重建
        // 6 缓存重建
        //  6.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        
        boolean isLock = tryLock(lockKey);
        // 6.2 判断是否获取成功
        if (isLock) {
            // 6.3 成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try {
                    // 查数据库
                    R r1 = dbFallback.apply(id);
                    // 写入redis
                    this.setWithLogicalExpire(key,r1,time,unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        
        // 6.4 失败，返回过期商铺信息
        return r;
    }
    
    
    /**
     * 获取锁
     * @param key
     * @return 成功返回true<br/>否则返回false
     */
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        
        return Boolean.TRUE.equals(flag);
    }
    
    /**
     * 删除锁
     * @param key
     */
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }
}
