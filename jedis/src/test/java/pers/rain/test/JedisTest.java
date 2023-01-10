package pers.rain.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pers.rain.jedis.utils.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * @Author W-ch
 * @Time 2023/1/9 13:42
 * @E-mail wang.xiaohong.0817@gamil.com
 * @File JedisTest .java
 * @Software IntelliJ IDEA
 */
public class JedisTest {
    private Jedis jedis;
    
    @BeforeEach
    void setUp(){
        // 建立连接
//        jedis = new Jedis("47.113.177.50",6379);
        jedis = JedisConnectionFactory.getJedis();
        
        // 设置密码
        jedis.auth("Wch@0817");
        
        // 选择库
        jedis.select(0);
    }
    
    @Test
    void testString(){
        // 存数据
        String result = jedis.set("name", "小王");
        System.out.println(result);
        
        // 取数据
        String name = jedis.get("name");
        System.out.println(name);
    }
    @Test
    void testHash(){
        // 存数据
        jedis.hset("user:1", "name","Jack");
        jedis.hset("user:1", "age","18");
        
        Map<String, String> map = jedis.hgetAll("user:1");
    
        System.out.println(map);
    }
    
    @AfterEach
    void tearDown(){
        if (jedis != null){
            jedis.close();
        }
    }
}
