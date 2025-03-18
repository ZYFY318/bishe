package com.example.houduan.controller;

import com.example.houduan.pojo.ExamResult;
import com.example.houduan.service.ExamResultService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-results")
public class ExamResultController {

    private final ExamResultService examResultService;

    public ExamResultController(ExamResultService examResultService) {
        this.examResultService = examResultService;
    }

    @GetMapping("/user/{userId}")
    public List<ExamResult> getUserExamResults(@PathVariable Integer userId) {
        return examResultService.getUserExamResults(userId);
    }

    @PostMapping("/save")
    public ExamResult saveExamResult(@RequestBody ExamResult examResult) {
        return examResultService.saveExamResult(examResult);
    }
}
