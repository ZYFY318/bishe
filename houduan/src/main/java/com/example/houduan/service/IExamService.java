package com.example.houduan.service;

import com.example.houduan.pojo.Exam;
import com.example.houduan.pojo.dto.ExamDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IExamService {
    
    /**
     * 获取所有试卷
     */
    List<Exam> getAllExams();
    
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
} 