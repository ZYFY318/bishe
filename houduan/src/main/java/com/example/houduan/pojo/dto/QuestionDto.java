package com.example.houduan.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor  // 自动生成一个包含所有字段的构造函数
public class QuestionDto {

    private String title;
    private List<String> options;
    private String answer;
}