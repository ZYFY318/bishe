package com.example.houduan.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求数据
 */
@Data
public class RegisterRequestDto {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "用户类型不能为空")
    private String userType;
    
    private String email;
} 