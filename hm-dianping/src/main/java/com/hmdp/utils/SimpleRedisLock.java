package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.objenesis.instantiator.perc.PercInstantiator;

import java.util.concurrent.TimeUnit;

/**
 * @Author W-ch
 * @Time 2023/1/16 10:47
 * @E-mail wang.xiaohong.0817@gmail.com
 * @File SimpleRedisLock .java
 * @Software IntelliJ IDEA
 */
public class SimpleRedisLock implements ILock{
    // 锁名称
    private String name;
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String KEY_PREFIX = "lock:";
    
    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标识
        long threadId = Thread.currentThread().getId();
        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                                  .setIfAbsent(KEY_PREFIX + name, String.valueOf(threadId), timeoutSec, TimeUnit.SECONDS);
    
        return Boolean.TRUE.equals(success);
    }
    
    @Override
    public void unlock() {
        // 释放锁
        stringRedisTemplate.delete(KEY_PREFIX + name);
    }
}
