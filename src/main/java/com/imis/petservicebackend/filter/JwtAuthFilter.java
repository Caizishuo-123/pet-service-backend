package com.imis.petservicebackend.filter;

import com.imis.petservicebackend.utlis.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // 白名单前缀
    private static final List<String> WHITE_LIST_PREFIX = Arrays.asList(
            "/user/login",
            "/user/register",
            "/user/logout");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        System.out.println("请求 URI = " + uri);

        // 白名单放行
        boolean isWhite = WHITE_LIST_PREFIX.stream().anyMatch(uri::startsWith);
        if (isWhite) {
            filterChain.doFilter(request, response);
            return;
        }

        // 拦截所有非白名单请求
        if (!uri.startsWith("")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"msg\":\"未授权，缺少Token\"}");
            return;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"msg\":\"Token无效或已过期\"}");
            return;
        }

        // 校验通过
        // 将用户信息存入 request，方便后续 Controller 直接获取
        String account = JwtUtil.getUserAccount(token);
        request.setAttribute("account", account);

        filterChain.doFilter(request, response);
    }
}
