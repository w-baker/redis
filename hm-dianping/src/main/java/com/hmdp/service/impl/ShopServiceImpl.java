package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import io.netty.util.internal.StringUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveStreamCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CacheClient cacheClient;
    @Override
    public Result queryById(Long id) {
        // 缓存穿透
        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY , id, Shop.class, CACHE_SHOP_TTL, TimeUnit.MINUTES, this::getById);
        
        // 互斥锁解决缓存击穿
//        Shop shop = queryWhitMutex(id);
        
        // 逻辑过期解决缓存击穿
//        Shop shop = cacheClient.queryWhitLogicalExpire(CACHE_SHOP_KEY,id,Shop.class,this::getById,CACHE_SHOP_TTL, TimeUnit.MINUTES);
        
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }
    
//    public Shop queryWhitLogicalExpire(Long id){
//        Gson gson = new Gson();
//        String key = CACHE_SHOP_KEY + id;
//
//        // 1.从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2.判断是否存在
//        if (StrUtil.isBlank(shopJson)) {
//
//            // 3 不存在，则返回
//            return null;
//        }
//        //4 命中，反序列化为RedisData
//        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//
//        JSONObject data = (JSONObject) redisData.getData();
//        Shop shop = JSONUtil.toBean(data, Shop.class);
//
//        LocalDateTime expireTime = redisData.getExpireTime();
//        // 5 判断是否过期
//        if (expireTime.isAfter(LocalDateTime.now())) {
//            // 5.1 未过期，返回店铺信息
//            return shop;
//        }
//        // 5.2 过期，进行缓存重建
//        // 6 缓存重建
//        //  6.1 获取互斥锁
//        String lockKey = LOCK_SHOP_KEY + id;
//
//        boolean isLock = tryLock(lockKey);
//        // 6.2 判断是否获取成功
//        if (isLock) {
//            // 6.3 成功，开启独立线程，实现缓存重建
//            CACHE_REBUILD_EXECUTOR.submit(()->{
//                try {
//                    // 重建
//                    saveShop2Redis(id,20L);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }finally {
//                    // 释放锁
//                    unlock(lockKey);
//                }
//            });
//        }
//
//        //  6.4 失败，返回过期商铺信息
//        return shop;
//    }
//
//    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
//
//    /**
//     * 互斥锁解决缓存击穿
//     * @param id
//     * @return
//     */
//    public Shop queryWhitMutex(Long id){
//        Gson gson = new Gson();
//        String key = CACHE_SHOP_KEY + id;
//        // 1.从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2.判断是否存在
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 2.1 存在，则返回
//            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
//            return shop;
//        }
//
//        // 判断是否为空值
//        if (Objects.equals(shopJson, "")) {
//            return null;
//        }
//        Shop shop = null;
//
//        try {
//            // 实现缓存重建
//            // ~。1 获取互斥锁
//            boolean isLock = tryLock(LOCK_SHOP_KEY);
//            // ~。2 判断是否获取成功
//            if (!isLock) {
//                // ~。3 失败，则休眠并且重试
//                Thread.sleep(50);
//                return queryWhitMutex(id);
//            }
//
//            // 2.2 不存在，则根据id查询数据库
//            shop = getById(id);
//            // 模拟延时
//            Thread.sleep(200);
//
//            // 3 不存在，返回错误
//            if (shop == null) {
//                // 3.1 将空值写入redis
//                stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//
//            // 3.1 存在，写入redis
//            shopJson = gson.toJson(shop);
//            stringRedisTemplate.opsForValue().set(key,shopJson,CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }finally {
//            // 释放互斥锁
//            unlock(LOCK_SHOP_KEY);
//        }
//
//        //  返回数据
//        return shop;
//    }
//    /**
//     * 缓存穿透实现
//     * @param id
//     * @return
//     */
//    public Shop queryWhitPassThrough(Long id){
//        String key = CACHE_SHOP_KEY + id;
//        // 1.从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        // 2.判断是否存在
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 2.1 存在，则返回
//            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
//            return shop;
//        }
//        // 判断是否为空值
//        if (Objects.equals(shopJson, "")) {
//            return null;
//        }
//        // 2.2 不存在，则根据id查询数据库
//        Shop shop = getById(id);
//        // 3 不存在，返回错误
//        if (shop == null) {
//            // 3.1 将空值写入redis
//            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
//            return null;
//        }
//        // 3.1 存在，写入redis
//        Gson gson = new Gson();
//        shopJson = gson.toJson(shop);
//        stringRedisTemplate.opsForValue().set(key,shopJson,CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        // 2.2 返回数据
//        return shop;
//    }
//
//    /**
//     * 获取锁
//     * @param key
//     * @return 成功返回true<br/>否则返回false
//     */
//    private boolean tryLock(String key){
//        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
//
//        return Boolean.TRUE.equals(flag);
//    }
//
//    /**
//     * 删除锁
//     * @param key
//     */
//    private void unlock(String key){
//        stringRedisTemplate.delete(key);
//    }
//
//
//    public void saveShop2Redis(Long id,Long expireSeconds) throws InterruptedException {
//        // 查询店铺信息
//        Shop shop = getById(id);
//        Thread.sleep(200);
//        // 封装逻辑过期
//        RedisData redisData = new RedisData();
//
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//        // 写入redis
//        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id,JSONUtil.toJsonStr(redisData));
//    }
//
    
    @Override
    @Transactional
    public Result update(Shop shop) {
        if (shop == null) {
            return Result.fail("店铺id不能为空");
        }
        // 更新数据库
        updateById(shop);
        // 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }
}
