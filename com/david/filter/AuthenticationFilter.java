package com.david.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class AuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 原有的用户信息获取逻辑应该在这里
        Object userInfo = getUserInfoFromRequest(request); // 这里是获取用户信息的逻辑
        
        if (userInfo != null) {
            // 用户信息存在，继续处理请求
            filterChain.doFilter(request, response);
        } else {
            // 用户信息未找到的处理逻辑 - 修改后的代码
            handleMissingUserInfo(request, response, filterChain);
        }
    }
    
    /**
     * 处理用户信息未找到的情况
     */
    private void handleMissingUserInfo(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws IOException, ServletException {
        // 方案1: 返回401未授权状态
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Unauthorized: User information not found\"}");
        
        // 方案2: 或者允许匿名访问（根据业务需求选择）
        // filterChain.doFilter(request, response);
        
        // 方案3: 重定向到登录页面
        // response.sendRedirect("/login");
    }
    
    /**
     * 模拟从请求中获取用户信息的方法
     */
    private Object getUserInfoFromRequest(HttpServletRequest request) {
        // 实际的用户信息获取逻辑应该在这里实现
        // 例如从header、cookie、session等获取用户信息
        return null; // 示例中返回null触发错误处理
    }
}