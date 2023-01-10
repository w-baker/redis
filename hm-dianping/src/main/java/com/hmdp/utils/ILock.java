package com.hmdp.utils;

/**
 * @Author W-ch
 * @Time 2023/1/16 10:45
 * @E-mail wang.xiaohong.0817@gmail.com
 * @File ILock .java
 * @Software IntelliJ IDEA
 */
public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec 锁的超时时间
     * @return true：获取锁成功<br/>false：代表获取锁失败
     */
    boolean tryLock(long timeoutSec);
    
    /**
     * 释放锁
     */
    void unlock();
}
