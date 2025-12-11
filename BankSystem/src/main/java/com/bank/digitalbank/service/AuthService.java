// com.bank.digitalbank.service.AuthService.java
package com.bank.digitalbank.service;

import com.bank.digitalbank.dto.auth.*;
import com.bank.digitalbank.entity.User;
import com.bank.digitalbank.entity.PasswordResetToken;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    User getCurrentUser();
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    String generateJwtToken(User user);
    boolean validateJwtToken(String token);
    User getUserFromToken(String token);
}