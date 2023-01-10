package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.objenesis.instantiator.perc.PercInstantiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

@SpringBootTest
@Slf4j
class HmDianPingApplicationTests {
    @Autowired
    private ShopServiceImpl shopService;
    
    @Autowired
    private CacheClient cacheClient;
    
    @Autowired
    private RedisIdWorker redisIdWorker;
    
    private ExecutorService es = Executors.newFixedThreadPool(500);
    
    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100;i++){
                long id = redisIdWorker.newtId("order");
                log.info("id = " + id);
            }
            latch.countDown();
        };
        long start = System.currentTimeMillis();
        for (int i = 0;i < 300;i++){
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        log.warn("耗时：" + (end-start) + "ms");
    }
    
    @Test
    public void testSaveShop2Redis() throws InterruptedException {
        Shop  shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY + 1L,shop,10L, TimeUnit.SECONDS);
    }
    
    

}
