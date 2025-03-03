package com.example.houduan.service;

import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.UserDto;

public interface IUserService {
    User add(UserDto user);

    //查询用户
    User getUser(Integer userId);

    User editUser(UserDto user);

    void deleteUser(Integer userId);

    User authenticate(String username, String password);
    String generateToken(User user);
    User getUserByToken(String token);
}
