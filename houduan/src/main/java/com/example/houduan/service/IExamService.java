package com.example.houduan.service;

import com.example.houduan.pojo.Exam;
import com.example.houduan.pojo.dto.ExamDto;
import com.example.houduan.pojo.dto.ExamQuestionsDto;
import com.example.houduan.pojo.dto.ExamQuestionsResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IExamService {
    
    /**
     * 获取所有试卷
     */
    List<Exam> getAllExams();
    
    /**
     * 获取特定教师创建的试卷
     */
    List<Exam> getExamsByCreatorId(Integer creatorId);
    
    /**
     * 根据ID获取试卷
     */
    Exam getExamById(Integer id);
    
    /**
     * 创建新试卷
     */
    Exam createExam(ExamDto examDto, MultipartFile cover);
    
    /**
     * 更新试卷
     */
    Exam updateExam(Integer id, ExamDto examDto, MultipartFile cover);
    
    /**
     * 删除试卷
     */
    void deleteExam(Integer id);
    
    /**
     * 获取试卷中的题目
     */
    ExamQuestionsResponseDto getExamQuestions(Integer examId);
    
    /**
     * 向试卷添加题目
     */
    Exam addQuestionsToExam(Integer examId, ExamQuestionsDto examQuestionsDto);
    
    /**
     * 从试卷中移除题目
     */
    void removeQuestionFromExam(Integer examId, Long questionId);
    
    /**
     * 发布或取消发布试卷
     */
    Exam publishExam(Integer examId, Boolean published);
    
    /**
     * 获取所有已发布的试卷（适用于学生）
     */
    List<Exam> getPublishedExams();
} 