package com.example.houduan.service;

import com.example.houduan.pojo.QuestionAnalysis;
import java.util.List;

public interface IQuestionAnalysisService {
    /**
     * 获取考试题目分析列表
     */
    List<QuestionAnalysis> getAnalysisByExamId(Integer examId);
    
    /**
     * 获取特定题目的分析
     */
    QuestionAnalysis getAnalysisByQuestionId(Integer examId, Long questionId);
    
    /**
     * 更新题目分析
     */
    QuestionAnalysis updateQuestionAnalysis(QuestionAnalysis analysis);
    
    /**
     * 更新考试的所有题目分析
     */
    List<QuestionAnalysis> updateExamQuestionAnalysis(Integer examId);
    
    /**
     * 删除题目分析
     */
    void deleteAnalysis(Long id);
    
    /**
     * 删除考试的所有题目分析
     */
    void deleteByExamId(Integer examId);
} 