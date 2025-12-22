package com.digitalbank.service;

import com.digitalbank.dto.request.LoginRequest;
import com.digitalbank.dto.request.RegisterRequest;
import com.digitalbank.dto.response.ApiResponse;

public interface AuthService {
    /**
     * 用户注册
     */
    ApiResponse<?> register(RegisterRequest request);

    /**
     * 用户登录
     */
    ApiResponse<?> login(LoginRequest request);
}