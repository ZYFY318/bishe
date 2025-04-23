package com.example.houduan.repository;

import com.example.houduan.pojo.QuestionAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionAnalysisRepository extends JpaRepository<QuestionAnalysis, Long> {
    /**
     * 根据考试ID查询所有题目统计数据
     */
    List<QuestionAnalysis> findByExamId(Integer examId);
    
    /**
     * 根据考试ID和题目ID查询特定题目统计数据
     */
    Optional<QuestionAnalysis> findByExamIdAndQuestionId(Integer examId, Long questionId);
    
    /**
     * 删除考试的所有题目分析
     */
    void deleteByExamId(Integer examId);
} 