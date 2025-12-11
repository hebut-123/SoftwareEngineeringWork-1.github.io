package com.bank.digitalbank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * JWT认证过滤器（仅依赖JwtUtil和UserDetailsService，消除循环依赖）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    // 可选：注入异常解析器，统一处理过滤器中的异常
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 跳过公开接口（如登录、注册），提升性能
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/") || requestURI.startsWith("/api/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 从请求头中提取JWT Token
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim(); // 去除可能的空格
                username = jwtUtil.extractUsername(token);
            }

            // 2. 校验Token有效性和认证状态
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 3. 加载用户详情
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 4. 验证Token是否有效
                if (jwtUtil.validateToken(token, userDetails)) {
                    // 5. 创建认证Token并设置到SecurityContext
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    // 设置请求详情（如IP地址、会话ID）
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 将认证信息存入SecurityContext，完成认证
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("用户 {} 认证成功，URI: {}", username, requestURI);
                }
            }

            // 6. 继续执行过滤器链
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT认证过滤器处理异常", e);
            // 统一异常处理（避免过滤器中抛出异常导致请求中断）
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    /**
     * 可选：指定过滤器是否异步执行（默认false）
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /**
     * 可选：指定是否过滤错误请求（默认false）
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}