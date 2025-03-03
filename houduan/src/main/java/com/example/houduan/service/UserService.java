package com.example.houduan.service;

import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.UserDto;
import com.example.houduan.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static javax.crypto.Cipher.SECRET_KEY;

@Service
public class UserService implements IUserService{

    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
     @Override
    public User add(UserDto user) {
         User userPojo = new User();
         BeanUtils.copyProperties(user, userPojo);
         return userRepository.save(userPojo);
         //调用数据访问类
     }

    @Override
    public User getUser(Integer userId) {
         return userRepository.findById(userId).orElseThrow(() -> {
             throw new IllegalArgumentException("用户不存在 参数异常");
         });

    }

    @Override
    public User editUser(UserDto user) {
         User userPojo = new User();
         BeanUtils.copyProperties(user, userPojo);
         return userRepository.save(userPojo);
    }

    @Override
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public User authenticate(String username, String password) {
        // 根据用户名查询用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));


        // 验证密码（需加密存储，此处假设密码已加密）
        System.out.println(password );
        System.out.println(user.getPassword());
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//            throw new BadCredentialsException("密码错误");
//        }
        if(!password.equals(user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        return user;
    }

    //---------------- Token生成（推荐JWT） ----------------
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256); // 自动生成安全密钥
    @Override
    public String generateToken(User user) {
        // 生成JWT Token（需集成JWT库，如jjwt）
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1小时过期
                .signWith(SECRET_KEY)
                .compact();
    }

    //---------------- 根据Token获取用户 ----------------
    public User getUserByToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY) // 使用正确的密钥
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Integer userId = claims.get("userId", Integer.class);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        } catch (JwtException e) {
            throw new IllegalArgumentException("无效 Token");
        }
    }
}
