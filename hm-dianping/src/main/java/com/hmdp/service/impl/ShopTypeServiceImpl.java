package com.hmdp.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_TYPE_LIST_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_TYPE_LIST_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        Gson gson = new Gson();
        // 从redis中查询
        String listJson = stringRedisTemplate.opsForValue().get(CACHE_TYPE_LIST_KEY);
        // 存在，则返回
        if (Strings.isNotBlank(listJson)) {
            List<ShopType> shopTypeList = gson.fromJson(listJson, new TypeToken<List<ShopType>>() {}.getType());
            return Result.ok(shopTypeList);
        }
        // 不存在则到数据库中查询
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        // 不存在，返回失败
        if (shopTypeList.size() == 0) {
            return Result.fail("查询类型列表失败");
        }
        // 存在，保存到redis中s
        String json = gson.toJson(shopTypeList);
    
        stringRedisTemplate.opsForValue().set(CACHE_TYPE_LIST_KEY,json,CACHE_TYPE_LIST_TTL, TimeUnit.MINUTES);
        // 返回数据
        return Result.ok(shopTypeList);
    }
}
