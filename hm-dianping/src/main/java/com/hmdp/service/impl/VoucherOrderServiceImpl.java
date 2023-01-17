package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1.查询
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始和结束
        // 获取开始时间
        LocalDateTime beginTime = voucher.getBeginTime();
        // 获取结束时间
        LocalDateTime endTime = voucher.getEndTime();
        
        if (LocalDateTime.now().isBefore(beginTime)) {
            // 尚未开始
            return Result.fail("秒杀尚未开始！");
        }
        
        if (LocalDateTime.now().isAfter(endTime)) {
            // 已经结束
            return Result.fail("秒杀已经结束！");
        }
        
        // 3.判断库存是否充足
        if (voucher.getStock() < 1) {
            return Result.fail("库存不足");
        }
        Long userId = UserHolder.getUser().getId();
        
//        synchronized (userId.toString().intern()) {
//            // 获取代理对象（事务）
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//        }
        
        
        // 创建锁对象
//        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
    
        RLock lock = redissonClient.getLock("order:" + userId);
        // 获取锁
        boolean isLock = lock.tryLock();
        
        // 判断锁是否获取成功
        if (!isLock) {
            // 获取失败，返回错误或重试
            return Result.fail("不允许重复下单");
        }
        
        // 获取锁成功
        try {
            // 获取代理对象（事务）
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }
    
    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        // 4.一人一单
        Long userId = UserHolder.getUser().getId();
        
        // 4.1查询订单
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        // 4.2判断是否存在
        if (count > 0) {
            return Result.fail("用户已经购买过一次！");
        }
        
        // 5.扣减库存
        boolean success = seckillVoucherService.update()
                                  .setSql("stock = stock - 1")
                                  .eq("voucher_id", voucherId)
                                  .gt("stock", 0) // 乐观锁
                                  .update();
        if (!success) {
            // 扣减失败
            return Result.fail("库存不足");
        }
        
        // 7。创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 7.1订单id
        long orderId = redisIdWorker.newtId("order");
        voucherOrder.setId(orderId);
        
        // 7.2用户id
        voucherOrder.setUserId(userId);
        
        // 7.3代金券id
        voucherOrder.setVoucherId(voucherId);
        // 8.返回订单id
        boolean save = save(voucherOrder);
        
        return Result.ok(orderId);
    }
}
