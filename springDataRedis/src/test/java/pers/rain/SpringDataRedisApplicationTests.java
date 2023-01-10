package pers.rain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import pers.rain.pojo.User;

@SpringBootTest
class SpringDataRedisApplicationTests {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    
    @Test
    void testString() {
        // 写入数据
        redisTemplate.opsForValue().set("name", "王");
        
        // 获取数据
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }
    
    @Test
    void testObject(){
        User user = new User("baker",17);
        
        redisTemplate.opsForValue().set("user:100", user);
    
        User o = (User) redisTemplate.opsForValue().get("user:100");
        System.out.println(user);
    }
}
