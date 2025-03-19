package com.example.houduan.controller;

import com.example.houduan.pojo.ExamResult;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.dto.ExamResultDto;
import com.example.houduan.pojo.dto.ExamResultResponseDto;
import com.example.houduan.service.ExamResultService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-results")
public class ExamResultController {

    private final ExamResultService examResultService;

    public ExamResultController(ExamResultService examResultService) {
        this.examResultService = examResultService;
    }

    // 获取用户的所有考试结果
    @GetMapping("/user/{userId}")
    public ResponseMessage<List<ExamResultResponseDto>> getUserExamResults(@PathVariable Integer userId) {
        List<ExamResultResponseDto> results = examResultService.getUserExamResults(userId);
        if (results.isEmpty()) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), "未找到考试成绩", "没有该用户的考试记录");
        }
//        System.out.println(results);
        return ResponseMessage.success(results);
    }

    // 保存考试成绩
    @PostMapping("/save")
    public ResponseMessage<ExamResultResponseDto> saveExamResult(@RequestBody ExamResultDto examResultDto) {
        try {
            ExamResultResponseDto savedExamResult = examResultService.saveExamResult(examResultDto);
            return ResponseMessage.success(savedExamResult);
        } catch (DataIntegrityViolationException e) {
            // 外键约束错误，说明user_id无效或不存在
            return ResponseMessage.error(HttpStatus.BAD_REQUEST.value(), "保存失败", "无效的用户ID或违反外键约束");
        } catch (Exception e) {
            // 其他异常
            return ResponseMessage.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "保存失败", "无法保存考试成绩");
        }
    }
}
