package com.digitalbank.service.impl;

import com.digitalbank.dto.request.RegisterRequest;
import com.digitalbank.dto.request.LoginRequest;
import com.digitalbank.dto.response.ApiResponse;
import com.digitalbank.entity.User;
import com.digitalbank.repository.UserRepository;
import com.digitalbank.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public ApiResponse<?> register(RegisterRequest request) {
        try {
            // 1. 唯一性验证
            if (userRepository.existsByNickname(request.getNickname())) {
                return ApiResponse.error("NICKNAME_EXISTS", "昵称已存在");
            }
            if (userRepository.existsByPhone(request.getPhone())) {
                return ApiResponse.error("PHONE_EXISTS", "手机号已注册");
            }
            if (userRepository.existsByIdCard(request.getIdCard())) {
                return ApiResponse.error("IDCARD_EXISTS", "身份证号已注册");
            }

            // 2. 密码一致性验证
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ApiResponse.error("PASSWORDS_NOT_MATCH", "两次密码不一致");
            }

            // 3. 创建用户
            User user = new User();
            user.setNickname(request.getNickname());
            user.setPhone(request.getPhone());
            user.setIdCard(request.getIdCard());
            user.setPassword(encodePassword(request.getPassword()));
            user.setCreateTime(LocalDateTime.now());

            user = userRepository.save(user);

            // 返回用户ID
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("nickname", user.getNickname());

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("SYSTEM_ERROR", "系统错误");
        }
    }

    @Override
    public ApiResponse<?> login(LoginRequest request) {
        try {
            // 1. 查找用户
            User user = userRepository.findByPhone(request.getPhone())
                    .orElse(null);
            if (user == null) {
                return ApiResponse.error("USER_NOT_FOUND", "用户不存在");
            }

            // 2. 验证密码
            if (!user.getPassword().equals(encodePassword(request.getPassword()))) {
                return ApiResponse.error("PASSWORD_ERROR", "密码错误");
            }

            // 3. 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("nickname", user.getNickname());
            data.put("phone", user.getPhone());
            // data.put("token", generateToken(user)); // 后续可以添加token

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("LOGIN_ERROR", "登录失败");
        }
    }

    // 密码加密（简化版）
    private String encodePassword(String password) {
        // TODO: 使用BCryptPasswordEncoder加密
        // return new BCryptPasswordEncoder().encode(password);
        return password; // 暂时返回原密码（仅用于演示）
    }
}