package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Objects;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 保存验证码到session
        session.setAttribute("phone", phone);
        session.setAttribute("code", code);
        // 发送验证码
        log.debug("发送验证码成功，手机号：{" + phone + "},验证码：{" + code + "}");
        
        return Result.ok();
    }
    
    @Override
    public Result longin(LoginFormDTO loginForm, HttpSession session) {
        // 获取输入的电话号码和验证码
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        
        // 获取生成的session中的电话号码和验证码
        String sessionPhone = (String) session.getAttribute("phone");
        String sessionCode = (String) session.getAttribute("code");
        
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        // 校验验证码,以及电话号码
        if (RegexUtils.isCodeInvalid(code) || !Objects.equals(sessionCode,code) || !Objects.equals(sessionPhone, phone)) {
            // 不一致，报错
            return Result.fail("验证码错误");
        }
        // 一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();
        // 判断用户是否存在
        if (user == null) {
            // 不存在，创建并保存
            user = createUserWithPhone(phone);
        }
        // 保存用户信息到session中
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        
        
        session.setAttribute("user",userDTO);
        return Result.ok();
    }
    
    private User createUserWithPhone(String phone) {
        // 创建用户
        User user = new User();
        user.setPhone(phone);
        
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        
        save(user);
        return user;
    }
}
