package com.example.houduan.controller;

import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.*;
import com.example.houduan.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController //接口方法返回对象转换为json文本
@RequestMapping("/user") //localhost:9090/user/**
@CrossOrigin
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
    
    /**
     * 用户信息更新（不含密码）
     * 用于个人中心更新个人信息
     */
    @PostMapping("/update")
    public ResponseMessage<User> updateUser(@Validated @RequestBody UserUpdateDto userUpdateDto) {
        User updatedUser = userService.updateUserInfo(userUpdateDto);
        return ResponseMessage.success(updatedUser, "用户信息更新成功");
    }
    
    /**
     * 文件上传接口 - 处理带有头像图片的用户信息更新
     */
    @PostMapping("/update/avatar")
    public ResponseMessage<User> updateUserWithAvatar(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer userId,
            @RequestParam("avatar") MultipartFile avatarFile) {
        
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setUsername(username);
        userUpdateDto.setEmail(email);
        userUpdateDto.setUserId(userId);
        
        User updatedUser = userService.updateUserWithAvatar(userUpdateDto, avatarFile);
        return ResponseMessage.success(updatedUser, "用户信息及头像更新成功");
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
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseMessage<User> register(@Validated @RequestBody RegisterRequestDto registerRequest) {
        try {
            User user = userService.register(registerRequest);
            return ResponseMessage.success(user, "注册成功");
        } catch (Exception e) {
            return ResponseMessage.error(e.getMessage());
        }
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
                user.getUserType(),
                user.getEmail(),
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

