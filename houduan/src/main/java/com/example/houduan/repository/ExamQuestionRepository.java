package com.example.houduan.repository;

import com.example.houduan.pojo.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    
    /**
     * 根据试卷ID查找关联的题目ID
     */
    List<ExamQuestion> findByExamId(Integer examId);
    
    /**
     * 根据试卷ID删除关联的题目
     */
    @Modifying
    void deleteByExamId(Integer examId);
    
    /**
     * 检查题目是否已存在于试卷中
     */
    boolean existsByExamIdAndQuestionId(Integer examId, Long questionId);
} 