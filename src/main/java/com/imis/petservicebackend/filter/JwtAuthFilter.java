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
            "/admin/auth/login",
            "/admin/auth/register",
            "/admin/auth/logout"
    );

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

        // 只拦 /admin/**
        if (!uri.startsWith("/admin")) {
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
        filterChain.doFilter(request, response);
    }
}
