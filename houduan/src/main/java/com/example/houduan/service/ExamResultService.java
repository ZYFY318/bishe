package com.example.houduan.service;

import com.example.houduan.pojo.ExamResult;
import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.ExamResultDto;
import com.example.houduan.pojo.dto.ExamResultResponseDto;
import com.example.houduan.repository.ExamResultRepository;
import com.example.houduan.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamResultService {
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;

    public ExamResultService(ExamResultRepository examResultRepository, UserRepository userRepository) {
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
    }

    // 查询时返回不带User的DTO列表
    public List<ExamResultResponseDto> getUserExamResults(Integer userId) {
        return examResultRepository.findByUserId(userId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // 保存时返回不带User的DTO
    public ExamResultResponseDto saveExamResult(ExamResultDto examResultDTO) {
        ExamResult examResult = new ExamResult();
        BeanUtils.copyProperties(examResultDTO, examResult);
        examResult.setUserId(examResultDTO.getUserId());

        ExamResult savedResult = examResultRepository.save(examResult);
        return convertToResponseDTO(savedResult);
    }

    // 实体转响应DTO的转换方法
    private ExamResultResponseDto convertToResponseDTO(ExamResult examResult) {
        ExamResultResponseDto dto = new ExamResultResponseDto();
        BeanUtils.copyProperties(examResult, dto);
        return dto;
    }
}
