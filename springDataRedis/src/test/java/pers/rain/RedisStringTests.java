package pers.rain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import pers.rain.pojo.User;

import java.util.Map;

@SpringBootTest
class RedisStringTests {
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Test
    void testString() {
        // 写入数据
        redisTemplate.opsForValue().set("name", "王");
        
        // 获取数据
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }
    
    private static final ObjectMapper mapper = new ObjectMapper();
    @Test
    void testObject() throws JsonProcessingException {
        // 创建对象
        User user = new User("baker",17);
        
        // 序列化
        String json = mapper.writeValueAsString(user);
        System.out.println(json);
    
        redisTemplate.opsForValue().set("user:200", json);
    
        String jsonString = redisTemplate.opsForValue().get("user:200");
    
        User user1 = mapper.readValue(jsonString, User.class);
        System.out.println(user1);
    }
    @Test
    void testHash(){
        redisTemplate.opsForHash().put("user:300","name","baker");
        redisTemplate.opsForHash().put("user:300","age","20");
    
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("user:300");
        System.out.println("entries = " + entries);
    }
}
