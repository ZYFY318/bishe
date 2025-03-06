package com.example.houduan.controller;

import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.LoginRequestDto;
import com.example.houduan.pojo.dto.UserDto;
import com.example.houduan.pojo.dto.UserInfoDto;
import com.example.houduan.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController //接口方法返回对象转换为json文本
@RequestMapping("/user") //localhost:9090/user/**
public class UserController {
    @Autowired
    IUserService userService;
    //add
    @PostMapping
    public ResponseMessage<User> add(@Validated @RequestBody UserDto user) {
        User userNew = userService.add(user) ;
        return ResponseMessage.success(userNew);
    }


    //query
    @GetMapping("/{userId}")
    public ResponseMessage<User> get(@PathVariable Integer userId) {
        User userNew = userService.getUser(userId);
        return ResponseMessage.success(userNew);
    }

    //modified
    @PutMapping
    public ResponseMessage<User> edit(@Validated @RequestBody UserDto user) {
        User userNew = userService.editUser(user);
        return ResponseMessage.success(userNew);
    }

    //delete
    //@DeleteMapping
    @DeleteMapping("/{userId}")
    public ResponseMessage<User> delete(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseMessage.success();
    }


    @PostMapping("/login")
    public ResponseMessage<Map<String, String>> login(@RequestBody LoginRequestDto loginRequest) {
        // 1. 验证用户名和密码
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        // 2. 生成Token（此处简化，实际应使用JWT）
        String token = userService.generateToken(user);
        System.out.println(token);
        // 3. 返回Token
        return ResponseMessage.success(Collections.singletonMap("token", token));
    }
    @GetMapping("/info")
    public ResponseMessage<UserInfoDto> getUserInfo(@RequestHeader("token") String token) {
        // 1. 根据Token获取用户信息
        User user = userService.getUserByToken(token);
        // 2. 转换为前端需要的UserInfo格式
        UserInfoDto userInfo = new UserInfoDto(
                user.getUserId(),
                user.getUsername(),
                user.getAvatar(),
                user.getRoles(),
                user.getButtons(),
                user.getRoutes()
        );

        return ResponseMessage.success(userInfo);
    }
    @PostMapping("/logout")
    public ResponseMessage<String> logout() {
        return ResponseMessage.success("登出成功");
    }

}

