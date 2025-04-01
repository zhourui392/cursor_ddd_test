package com.example.demo.facade.dto;

/**
 * 统一API响应对象
 * @param <T> 响应数据类型
 */
public class ApiResponse<T> {
    /**
     * 状态码
     */
    private String code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    public ApiResponse() {
    }
    
    public ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 成功响应
     * @param data 数据
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("200", "操作成功", data);
    }
    
    /**
     * 成功响应
     * @param message 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("200", message, data);
    }
    
    /**
     * 错误响应
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    /**
     * 错误响应（默认错误码500）
     * @param message 错误消息
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("500", message, null);
    }
    
    /**
     * 参数错误响应
     * @param message 错误消息
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>("400", message, null);
    }
    
    /**
     * 未授权响应
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>("401", "未授权", null);
    }
    
    /**
     * 服务器错误响应
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> serverError() {
        return new ApiResponse<>("500", "服务器错误", null);
    }
    
    /**
     * 设置响应数据并返回当前实例
     * @param data 数据
     * @return 当前API响应实例
     */
    public ApiResponse<T> data(T data) {
        this.data = data;
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
} 