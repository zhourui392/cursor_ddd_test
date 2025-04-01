package com.example.demo.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * 注册用户命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserCommand {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度必须在6-40之间")
    private String password;

    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Size(max = 50, message = "邮箱长度不能超过50")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    private Set<String> roles;
} 