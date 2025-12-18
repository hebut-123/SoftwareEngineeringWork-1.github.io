// LoginResponse.java
package com.bank.digitalbank.dto.auth;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private UserResponse data;

    @Data
    public static class UserResponse {
        private String token;
        private UserInfo user;

        @Data
        public static class UserInfo {
            private Long id;
            private String username;
            private String fullname;
            private String email;
            private String phone;
        }
    }
}