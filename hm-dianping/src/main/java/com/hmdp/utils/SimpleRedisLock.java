package com.hmdp.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.objenesis.instantiator.perc.PercInstantiator;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
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
    /**
     * 锁名称前缀
     */
    private static final String KEY_PREFIX = "lock:";
    /**
     * 获取uuid
     */
    private static final String ID_PREFIX = UUID.randomUUID().toString().replace("-", "") + "-";
    
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        
        UNLOCK_SCRIPT.setResultType(Long.class);
    }
    
    
    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                                  .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
    
        return Boolean.TRUE.equals(success);
    }
    
    @Override
    public void unlock() {
        // 调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId()
                );
    }
    
//    @Override
//    public void unlock() {
//        // 获取线程标示
//        String threadId = ID_PREFIX + Thread.currentThread().getId();
//
//        // 获取锁中标识
//        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
//
//        if (Objects.equals(threadId, id)) {
//            // 释放锁
//            stringRedisTemplate.delete(KEY_PREFIX + name);
//        }
//    }
}
