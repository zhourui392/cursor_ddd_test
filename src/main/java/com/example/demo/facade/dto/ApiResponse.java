package com.example.demo.facade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API统一响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    
    private static final String SUCCESS_CODE = "200";
    private static final String SUCCESS_MESSAGE = "操作成功";
    private static final String ERROR_CODE = "500";
    private static final String ERROR_MESSAGE = "系统错误";
    
    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .data(data)
                .build();
    }
    
    /**
     * 创建成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
    
    /**
     * 创建系统错误响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code(ERROR_CODE)
                .message(message)
                .build();
    }
    
    /**
     * 设置响应数据
     */
    public ApiResponse<T> data(T data) {
        this.data = data;
        return this;
    }
} 