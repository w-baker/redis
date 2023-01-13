package com.hmdp;

import com.hmdp.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.objenesis.instantiator.perc.PercInstantiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HmDianPingApplicationTests {
    @Autowired
    private ShopServiceImpl shopService;
    
    @Test
    public void testSaveShop2Redis() throws InterruptedException {
        shopService.saveShop2Redis(1L, 10L);
    }

}
