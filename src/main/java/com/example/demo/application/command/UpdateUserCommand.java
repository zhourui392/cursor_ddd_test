package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 更新用户命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCommand {
    private Long id;
    
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Size(max = 50, message = "邮箱长度不能超过50")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;
    
    private Boolean status;
} 