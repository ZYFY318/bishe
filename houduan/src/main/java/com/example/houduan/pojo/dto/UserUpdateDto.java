package com.example.houduan.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户信息更新DTO，不包含密码字段
 * 用于用户个人信息更新场景
 */
@Data
public class UserUpdateDto {
    private Integer userId;
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    // 头像路径
    private String avatar;
    
    public UserUpdateDto() {
    }
    
    public UserUpdateDto(Integer userId, String username, String email, String avatar) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
    }
} 