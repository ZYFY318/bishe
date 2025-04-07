package com.example.houduan.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExamDto {
    private Integer id;
    
    @NotBlank(message = "试卷名称不能为空")
    private String title;
    
    @Min(value = 1, message = "考试时长必须大于0")
    private Integer duration;
    
    // 用于保存封面图片URL
    private String coverUrl;
    
    // 试卷创建者ID
    private Integer creatorId;

    
} 