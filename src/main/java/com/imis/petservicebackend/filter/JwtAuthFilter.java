package com.imis.petservicebackend.filter;

import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.utlis.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 白名单前缀
    private static final List<String> WHITE_LIST_PREFIX = Arrays.asList(
            "/user/login",
            "/user/register",
            "/user/logout",
            "/email/send-code",
            "/email/register",
            "/email/reset-password",
            "/pet/adoption",
            "/pet/breeds",
            "/pet/detail",
            "/petService/",
            "/post/page",
            "/post/hot",
            "/post/notice",
            "/post/detail",
            "/comment/page");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 白名单放行
        boolean isWhite = WHITE_LIST_PREFIX.stream().anyMatch(uri::startsWith);
        if (isWhite) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未授权，缺少Token");
            return;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            writeUnauthorized(response, "Token无效或已过期");
            return;
        }

        // 校验通过
        // 将用户信息存入 request，方便后续 Controller 直接获取
        String account = JwtUtil.getUserAccount(token);
        request.setAttribute("account", account);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.fail(401, message)));
    }
}
