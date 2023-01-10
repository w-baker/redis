package pers.rain.jedis.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Author W-ch
 * @Time 2023/1/9 13:59
 * @E-mail wang.xiaohong.0817@gamil.com
 * @File JedisConnectionFactory .java
 * @Software IntelliJ IDEA
 */
public class JedisConnectionFactory {
    private static final JedisPool jedisPool;
    
    static {
        // 配置连接池
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(8);
        jedisPoolConfig.setMaxIdle(8);
        jedisPoolConfig.setMinIdle(0);
        jedisPoolConfig.setMaxWaitMillis(1000);
        // 创建连接对象
        jedisPool = new JedisPool(jedisPoolConfig,"40.113.177.50", 6379,1000,"wch@0817");
    }
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }
}
