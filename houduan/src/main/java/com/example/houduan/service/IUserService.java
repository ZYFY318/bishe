package com.example.houduan.service;

import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.RegisterRequestDto;
import com.example.houduan.pojo.dto.UserDto;
import com.example.houduan.pojo.dto.UserUpdateDto;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    User add(UserDto user);

    //查询用户
    User getUser(Integer userId);

    User editUser(UserDto user);
    
    /**
     * 更新用户信息（不含密码）
     * @param userUpdateDto 用户更新信息DTO
     * @return 更新后的用户信息
     */
    User updateUserInfo(UserUpdateDto userUpdateDto);
    
    /**
     * 更新用户信息和头像
     * @param userUpdateDto 用户更新信息DTO
     * @param avatarFile 头像文件
     * @return 更新后的用户信息
     */
    User updateUserWithAvatar(UserUpdateDto userUpdateDto, MultipartFile avatarFile);

    void deleteUser(Integer userId);

    User authenticate(String username, String password);
    String generateToken(User user);
    User getUserByToken(String token);
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册成功的用户信息
     */
    User register(RegisterRequestDto registerRequest);
}
