package com.example.houduan.service;

import com.example.houduan.config.FileStorageConfig;
import com.example.houduan.exception.ResourceNotFoundException;
import com.example.houduan.pojo.Exam;
import com.example.houduan.pojo.dto.ExamDto;
import com.example.houduan.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ExamService implements IExamService {

    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Override
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    @Override
    public Exam getExamById(Integer id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在，ID: " + id));
    }

    @Override
    public Exam createExam(ExamDto examDto, MultipartFile cover) {
        Exam exam = new Exam();
        exam.setTitle(examDto.getTitle());
        exam.setDuration(examDto.getDuration());
        
        // 处理封面图片
        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageConfig.storeFile(cover);
            exam.setCoverUrl(coverUrl);
        }
        
        return examRepository.save(exam);
    }

    @Override
    public Exam updateExam(Integer id, ExamDto examDto, MultipartFile cover) {
        Exam exam = getExamById(id);
        
        // 更新标题
        if (examDto.getTitle() != null) {
            exam.setTitle(examDto.getTitle());
        }
        
        // 更新考试时长
        if (examDto.getDuration() != null) {
            exam.setDuration(examDto.getDuration());
        }
        
        // 更新封面图片
        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageConfig.storeFile(cover);
            exam.setCoverUrl(coverUrl);
        }
        
        return examRepository.save(exam);
    }

    @Override
    public void deleteExam(Integer id) {
        Exam exam = getExamById(id);
        examRepository.delete(exam);
    }
} 