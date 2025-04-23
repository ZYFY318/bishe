package com.example.houduan.service;

import com.example.houduan.pojo.ExamStatistics;
import com.example.houduan.pojo.ScoreDistribution;
import java.util.List;

public interface IExamStatisticsService {
    /**
     * 根据考试ID获取统计数据
     */
    ExamStatistics getStatisticsByExamId(Integer examId);
    
    /**
     * 获取所有统计数据
     */
    List<ExamStatistics> getAllStatistics();
    
    /**
     * 更新考试统计数据
     */
    ExamStatistics updateStatistics(Integer examId);
    
    /**
     * 删除统计数据
     */
    void deleteStatistics(Long id);
    
    /**
     * 获取考试分数分布
     */
    ScoreDistribution getScoreDistribution(Integer examId);
} 