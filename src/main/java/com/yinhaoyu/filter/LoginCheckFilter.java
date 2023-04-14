package com.yinhaoyu.filter;

import com.alibaba.fastjson.JSON;
import com.yinhaoyu.common.BaseContext;
import com.yinhaoyu.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Vastness
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        log.info("拦截到请求登录{}", request.getRequestURL());
        // 获取本次请求的地址
        String requestUrl = request.getRequestURI();
        // 获取本次放行的地址
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };
        // 如果不需要处理，直接放行
        if (checkPath(urls, requestUrl)) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("路径需要处理");
        // 判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getSession().getAttribute("user") != null) {
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("user"));
            filterChain.doFilter(request, response);
            return;
        }
        log.info("用户未登录");
        // 未登录则返回登录结果，通过流的方式向客户端响应结果
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
    }

    /**
     * 检查客户端请求的url是否和不需要过滤的url比较
     *
     * @param urls       被排除过滤的urls
     * @param requestUrl 本次客户端请求的地址
     * @return true: 匹配成功
     */
    private boolean checkPath(String[] urls, String requestUrl) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestUrl)) {
                return true;
            }
        }
        return false;
    }
}
