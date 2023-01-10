package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author W-ch
 * @Time 2023/1/10 16:15
 * @E-mail wang.xiaohong.0817@gmail.com
 * @File LoginInterceptor .java
 * @Software IntelliJ IDEA
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取session
        HttpSession session = request.getSession();
        // 获取session中的用户
        UserDTO user = (UserDTO) session.getAttribute("user");
        // 判断用户是否存在
        if (user == null) {
            // 不存在则,拦截,返回401状态码
            response.setStatus(401);
            return false;
        }
        // 存在，保存用户信息到TheadLocal
        UserHolder.saveUser(user);
        // 放行
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
