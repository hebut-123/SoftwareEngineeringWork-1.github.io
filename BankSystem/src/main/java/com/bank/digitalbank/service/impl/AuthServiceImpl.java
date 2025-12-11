package com.bank.digitalbank.service.impl;

import com.bank.digitalbank.dto.auth.*;
import com.bank.digitalbank.entity.User;
import com.bank.digitalbank.entity.PasswordResetToken;
import com.bank.digitalbank.repository.UserRepository;
import com.bank.digitalbank.repository.PasswordResetTokenRepository;
import com.bank.digitalbank.service.AuthService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication; // 正确的导入
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = generateJwtToken(user);

        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setMessage("登录成功");

        LoginResponse.UserResponse userResponse = new LoginResponse.UserResponse();
        userResponse.setToken(token);

        LoginResponse.UserResponse.UserInfo userInfo = new LoginResponse.UserResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setFullname(user.getFullname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());

        userResponse.setUser(userInfo);
        response.setData(userResponse);

        return response;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已注册");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        // 从SecurityContext中获取当前用户的认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. 校验认证信息是否存在
        if (authentication == null) {
            throw new RuntimeException("未认证：当前无有效认证信息");
        }

        // 2. 校验是否为匿名用户
        if (authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("未认证：当前为匿名用户");
        }

        // 3. 校验Principal类型并转换
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("认证信息异常：用户信息格式错误");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 4. 从数据库查询用户
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在：" + userDetails.getUsername()));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 获取当前用户
        User currentUser = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("该邮箱未注册"));

        // 删除旧的token
        tokenRepository.deleteByUser(user);

        // 生成新的token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24小时有效
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // 发送重置邮件
        sendResetEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("无效的token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("token已使用");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("token已过期");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    @Override
    public String generateJwtToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("fullname", user.getFullname())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validateJwtToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    @Override
    public User getUserFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
        } catch (JwtException e) {
            throw new RuntimeException("无效的token");
        }
    }

    private void sendResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("数字银行 - 密码重置");

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String emailContent = String.format("""
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2 style="color: #2196F3;">数字银行密码重置</h2>
                        <p>您好，我们收到了您的密码重置请求。</p>
                        <p>请点击下面的链接重置您的密码（24小时内有效）：</p>
                        <p>
                            <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 5px;">
                                重置密码
                            </a>
                        </p>
                        <p>如果您没有请求重置密码，请忽略此邮件。</p>
                        <hr style="border: none; border-top: 1px solid #eee;">
                        <p style="color: #666; font-size: 12px;">
                            此邮件由数字银行系统自动发送，请勿回复。
                        </p>
                    </div>
                    """, resetLink);

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
    }
}