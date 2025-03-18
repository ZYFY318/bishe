package com.example.houduan.service;

import com.example.houduan.pojo.ExamResult;
import com.example.houduan.repository.ExamResultRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExamResultService {

    private final ExamResultRepository examResultRepository;

    public ExamResultService(ExamResultRepository examResultRepository) {
        this.examResultRepository = examResultRepository;
    }

    public List<ExamResult> getUserExamResults(Integer userId) {
        return examResultRepository.findByUserUserId(userId);
    }

    public ExamResult saveExamResult(ExamResult examResult) {
        return examResultRepository.save(examResult);
    }
}
