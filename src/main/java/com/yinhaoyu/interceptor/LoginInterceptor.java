package com.yinhaoyu.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Vastness
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        log.info("拦截登录请求地址{}", request.getRequestURL());
        //判断用户是否登录，未登录重定向到登录页面
        if (request.getSession().getAttribute("employee") == null) {
            response.sendRedirect(request.getContextPath() + "/backend/page/login/login.html");
            return false;
        }
        return true;
    }
}
