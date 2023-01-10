package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        // 判断是否需要拦截
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            // 拦截，设置拦截状态码
            response.setStatus(401);
            return false;
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
