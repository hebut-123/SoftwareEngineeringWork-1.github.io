package com.digitalbank.service.impl;

import com.digitalbank.dto.request.LoginRequest;
import com.digitalbank.dto.request.RegisterRequest;
import com.digitalbank.dto.response.ApiResponse;
import com.digitalbank.entity.User;
import com.digitalbank.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    /* =========================================================
     *  register
     * =======================================================*/
    @Test
    @DisplayName("R01: 注册成功")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setNickname("Tom");
        req.setPhone("13812345678");
        req.setIdCard("411111199901011234");
        req.setPassword("123456");
        req.setConfirmPassword("123456");

        when(userRepository.existsByNickname("Tom")).thenReturn(false);
        when(userRepository.existsByPhone("13812345678")).thenReturn(false);
        when(userRepository.existsByIdCard("411111199901011234")).thenReturn(false);

        User saved = new User();
        saved.setId(1L);
        saved.setNickname("Tom");
        saved.setCreateTime(LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        ApiResponse<?> resp = authService.register(req);

        assertTrue(resp.isSuccess());
        assertEquals(1L, ((Map<?, ?>) resp.getData()).get("userId"));
    }

    @Test
    @DisplayName("R02: 注册失败——昵称已存在")
    void register_nicknameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setNickname("Tom");

        when(userRepository.existsByNickname("Tom")).thenReturn(true);

        ApiResponse<?> resp = authService.register(req);

        assertFalse(resp.isSuccess());
        assertEquals("NICKNAME_EXISTS", resp.getCode());
    }

    @Test
    @DisplayName("R03: 注册失败——手机号已注册")
    void register_phoneExists() {
        RegisterRequest req = new RegisterRequest();
        req.setNickname("NewTom");
        req.setPhone("13812345678");

        when(userRepository.existsByNickname("NewTom")).thenReturn(false);
        when(userRepository.existsByPhone("13812345678")).thenReturn(true);

        ApiResponse<?> resp = authService.register(req);

        assertFalse(resp.isSuccess());
        assertEquals("PHONE_EXISTS", resp.getCode());
    }

    @Test
    @DisplayName("R04: 注册失败——两次密码不一致")
    void register_passwordNotMatch() {
        RegisterRequest req = new RegisterRequest();
        req.setNickname("Tom");
        req.setPhone("13800000000");
        req.setIdCard("411111199901011235");
        req.setPassword("123456");
        req.setConfirmPassword("654321");

        when(userRepository.existsByNickname("Tom")).thenReturn(false);
        when(userRepository.existsByPhone("13800000000")).thenReturn(false);
        when(userRepository.existsByIdCard("411111199901011235")).thenReturn(false);

        ApiResponse<?> resp = authService.register(req);

        assertFalse(resp.isSuccess());
        assertEquals("PASSWORDS_NOT_MATCH", resp.getCode());
    }

    /* =========================================================
     *  login
     * =======================================================*/
    @Test
    @DisplayName("L01: 登录成功")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setPhone("13812345678");
        req.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setNickname("Tom");
        user.setPhone("13812345678");
        user.setPassword("123456");
        when(userRepository.findByPhone("13812345678")).thenReturn(Optional.of(user));

        ApiResponse<?> resp = authService.login(req);

        assertTrue(resp.isSuccess());
        assertEquals("Tom", ((Map<?, ?>) resp.getData()).get("nickname"));
    }

    @Test
    @DisplayName("L02: 登录失败——用户不存在")
    void login_userNotFound() {
        LoginRequest req = new LoginRequest();
        req.setPhone("13800000000");
        when(userRepository.findByPhone("13800000000")).thenReturn(Optional.empty());

        ApiResponse<?> resp = authService.login(req);

        assertFalse(resp.isSuccess());
        assertEquals("USER_NOT_FOUND", resp.getCode());
    }

    @Test
    @DisplayName("L03: 登录失败——密码错误")
    void login_wrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setPhone("13812345678");
        req.setPassword("wrong");

        User user = new User();
        user.setPassword("right");
        when(userRepository.findByPhone("13812345678")).thenReturn(Optional.of(user));

        ApiResponse<?> resp = authService.login(req);

        assertFalse(resp.isSuccess());
        assertEquals("PASSWORD_ERROR", resp.getCode());
    }
}