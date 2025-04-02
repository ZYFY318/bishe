package com.example.houduan.pojo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_model")
@Data
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    private String imageUrl;
    private String description;
    private String glbPath;
} 