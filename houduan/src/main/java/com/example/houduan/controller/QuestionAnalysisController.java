package com.example.houduan.controller;

import com.example.houduan.pojo.QuestionAnalysis;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.service.IQuestionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question-analysis")
@CrossOrigin
public class QuestionAnalysisController {

    @Autowired
    private IQuestionAnalysisService questionAnalysisService;

    /**
     * 获取考试的所有题目分析
     */
    @GetMapping("/exam/{examId}")
    public ResponseMessage<List<QuestionAnalysis>> getAnalysisByExamId(@PathVariable Integer examId) {
        List<QuestionAnalysis> analyses = questionAnalysisService.getAnalysisByExamId(examId);
        if (analyses.isEmpty()) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), "未找到题目分析数据", "该考试暂无题目分析数据");
        }
        return ResponseMessage.success(analyses, "获取题目分析数据成功");
    }

    /**
     * 获取特定题目的分析
     */
    @GetMapping("/exam/{examId}/question/{questionId}")
    public ResponseMessage<QuestionAnalysis> getAnalysisByQuestionId(
            @PathVariable Integer examId,
            @PathVariable Long questionId) {
        try {
            QuestionAnalysis analysis = questionAnalysisService.getAnalysisByQuestionId(examId, questionId);
            return ResponseMessage.success(analysis, "获取题目分析数据成功");
        } catch (Exception e) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), "未找到题目分析数据", e.getMessage());
        }
    }

    /**
     * 更新考试的所有题目分析
     */
    @PostMapping("/update/{examId}")
    public ResponseMessage<List<QuestionAnalysis>> updateExamQuestionAnalysis(@PathVariable Integer examId) {
        try {
            List<QuestionAnalysis> analyses = questionAnalysisService.updateExamQuestionAnalysis(examId);
            if (analyses.isEmpty()) {
                return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), "更新题目分析失败", "没有可用的题目或考试记录");
            }
            return ResponseMessage.success(analyses, "更新题目分析数据成功");
        } catch (Exception e) {
            return ResponseMessage.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新题目分析失败", e.getMessage());
        }
    }

    /**
     * 删除题目分析
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteAnalysis(@PathVariable Long id) {
        try {
            questionAnalysisService.deleteAnalysis(id);
            return ResponseMessage.success(null, "删除题目分析数据成功");
        } catch (Exception e) {
            return ResponseMessage.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "删除题目分析失败", e.getMessage());
        }
    }

    /**
     * 删除考试的所有题目分析
     */
    @DeleteMapping("/exam/{examId}")
    public ResponseMessage<Void> deleteByExamId(@PathVariable Integer examId) {
        try {
            questionAnalysisService.deleteByExamId(examId);
            return ResponseMessage.success(null, "删除考试所有题目分析数据成功");
        } catch (Exception e) {
            return ResponseMessage.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "删除考试所有题目分析失败", e.getMessage());
        }
    }
} 