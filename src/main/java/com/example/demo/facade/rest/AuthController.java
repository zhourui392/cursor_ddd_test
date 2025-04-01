package com.example.demo.facade.rest;

import com.example.demo.application.command.LoginCommand;
import com.example.demo.application.command.RegisterUserCommand;
import com.example.demo.application.dto.UserDTO;
import com.example.demo.application.service.UserApplicationService;
import com.example.demo.facade.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserApplicationService userApplicationService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterUserCommand command) {
        UserDTO user = userApplicationService.registerUser(command);
        return new ResponseEntity<>(ApiResponse.success(user), HttpStatus.CREATED);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginCommand command) {
        String token = userApplicationService.login(command);
        
        // 记录登录时间
        userApplicationService.updateLastLoginTime(command.getUsername());
        
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenType", "Bearer");
        
        return ResponseEntity.ok(ApiResponse.success(tokenMap));
    }
}