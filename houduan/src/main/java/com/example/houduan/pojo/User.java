package com.example.houduan.pojo;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.List;
@Entity
@Table(name = "tb_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String username;
    private String password;
    private String avatar;

    @ElementCollection // 存储集合字段（如roles、buttons）
    private List<String> roles;
    @ElementCollection
    private List<String> buttons;
    @ElementCollection
    private List<String> routes;

    private String token; // 可选：如果需持久化Token（推荐使用JWT替代）
}
