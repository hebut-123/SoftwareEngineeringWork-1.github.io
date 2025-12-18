package com.digitalbank.service;

import com.digitalbank.dto.LoginDTO;
import com.digitalbank.entity.User;
import com.digitalbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> login(LoginDTO loginDTO) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElse(null);

        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 简化验证，实际项目中应该使用加密密码验证
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 生成token（简化版）
        String token = "TOKEN-" + System.currentTimeMillis() + "-" + user.getUserId();

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", user);

        return result;
    }

    public boolean validateToken(String token) {
        // 简化验证，实际项目中应该使用JWT等机制
        return token != null && token.startsWith("TOKEN-");
    }
}