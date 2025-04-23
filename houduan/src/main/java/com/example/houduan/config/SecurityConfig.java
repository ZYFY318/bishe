package com.example.houduan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用CSRF（前后端分离项目通常不需要）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user","/user/**").permitAll() // 放行登录接口
//                        .requestMatchers("/user/info").permitAll()  // ✅ 要求认证
                        .requestMatchers("/question,","/question/**").permitAll()
                        .requestMatchers("/exam-results,","/exam-results/**").permitAll()
                        .requestMatchers("/model,", "/model/**").permitAll()
                        .requestMatchers("/exams,", "/exams/**").permitAll()
                                .requestMatchers("/exam-statistics,", "exam-statistics/**").permitAll()
                                .requestMatchers("/question-analysis,", "question-analysis/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll() // 允许访问上传的文件
                        .anyRequest().authenticated() // 其他接口需认证
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}