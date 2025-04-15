package com.example.houduan.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserInfoDto {
    private Integer userId;
    private String username;
    private String avatar;
    private String userType;
    private String email;
    private List<String> roles;
    private List<String> buttons;
    private List<String> routes;
}