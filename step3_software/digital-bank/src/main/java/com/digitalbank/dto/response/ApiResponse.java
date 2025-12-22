package com.digitalbank.dto.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;            // 是否成功
    private String code;                // 状态码
    private String message;             // 消息
    private T data;                     // 数据

    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode("SUCCESS");
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    // 失败响应
    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

}