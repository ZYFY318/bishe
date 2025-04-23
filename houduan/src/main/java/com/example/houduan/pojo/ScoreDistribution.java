package com.example.houduan.pojo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScoreDistribution {
    private Integer examId; // 考试ID
    
    private Integer totalParticipants; // 总参与人数
    
    private List<Integer> sections = new ArrayList<>(); // 分数段划分
    
    private List<Integer> counts = new ArrayList<>(); // 每个分数段的人数
    
    private List<Double> percentages = new ArrayList<>(); // 每个分数段的百分比
    
    // 添加构造方法,可以设置默认分数段
    public ScoreDistribution() {
        // 默认按照10分为一段进行划分: 0-10, 11-20, ..., 91-100
        for (int i = 0; i <= 100; i += 10) {
            sections.add(i);
        }
        
        // 初始化计数和百分比
        for (int i = 0; i < sections.size(); i++) {
            counts.add(0);
            percentages.add(0.0);
        }
    }
} 