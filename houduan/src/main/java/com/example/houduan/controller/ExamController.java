package com.example.houduan.controller;

import com.example.houduan.pojo.Exam;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.dto.ExamDto;
import com.example.houduan.pojo.dto.ExamQuestionsDto;
import com.example.houduan.pojo.dto.ExamQuestionsResponseDto;
import com.example.houduan.service.IExamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exams")
@CrossOrigin
public class ExamController {

    @Autowired
    private IExamService examService;

    /**
     * 获取所有试卷
     */
    @GetMapping
    public ResponseMessage<List<Exam>> getAllExams() {
        List<Exam> exams = examService.getAllExams();
        return ResponseMessage.success(exams, "获取试卷列表成功");
    }

    /**
     * 获取试卷详情
     */
    @GetMapping("/{id}")
    public ResponseMessage<Exam> getExamById(@PathVariable Integer id) {
        Exam exam = examService.getExamById(id);
        return ResponseMessage.success(exam, "获取试卷详情成功");
    }

    /**
     * 获取特定教师创建的试卷
     */
    @GetMapping("/teacher/{creatorId}")
    public ResponseMessage<List<Exam>> getExamsByCreator(@PathVariable Integer creatorId) {
        List<Exam> exams = examService.getExamsByCreatorId(creatorId);
        return ResponseMessage.success(exams, "获取教师试卷列表成功");
    }

    /**
     * 创建试卷
     */
    @PostMapping("/create")
    public ResponseMessage<Exam> createExam(
            @RequestParam("title") String title,
            @RequestParam("duration") Integer duration,
            @RequestParam("creatorId") Integer creatorId,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        
        ExamDto examDto = new ExamDto();
        examDto.setTitle(title);
        examDto.setDuration(duration);
        examDto.setCreatorId(creatorId);
        
        Exam newExam = examService.createExam(examDto, cover);
        return ResponseMessage.success(newExam, "试卷创建成功");
    }

    /**
     * 更新试卷
     */
    @PutMapping("/{id}")
    public ResponseMessage<Exam> updateExam(
            @PathVariable Integer id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        
        ExamDto examDto = new ExamDto();
        examDto.setTitle(title);
        examDto.setDuration(duration);
        
        Exam updatedExam = examService.updateExam(id, examDto, cover);
        return ResponseMessage.success(updatedExam, "试卷更新成功");
    }

    /**
     * 删除试卷
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteExam(@PathVariable Integer id) {
        examService.deleteExam(id);
        return ResponseMessage.success(null, "试卷删除成功");
    }
    
    /**
     * 获取试卷中的题目
     */
    @GetMapping("/questions/{examId}")
    public ResponseMessage<ExamQuestionsResponseDto> getExamQuestions(@PathVariable Integer examId) {
        ExamQuestionsResponseDto questions = examService.getExamQuestions(examId);
        return ResponseMessage.success(questions, "获取试卷题目成功");
    }
    
    /**
     * 添加题目到试卷
     */
    @PostMapping("/questions/{examId}")
    public ResponseMessage<Exam> addQuestionsToExam(
            @PathVariable Integer examId,
            @RequestBody @Valid ExamQuestionsDto examQuestionsDto) {
        Exam exam = examService.addQuestionsToExam(examId, examQuestionsDto);
        return ResponseMessage.success(exam, "题目添加成功");
    }
    
    /**
     * 从试卷中移除题目
     */
    @DeleteMapping("/questions/{examId}/{questionId}")
    public ResponseMessage<Void> removeQuestionFromExam(
            @PathVariable Integer examId,
            @PathVariable Long questionId) {
        examService.removeQuestionFromExam(examId, questionId);
        return ResponseMessage.success(null, "题目移除成功");
    }
    
    /**
     * 发布或取消发布试卷
     */
    @PutMapping("/publish/{examId}")
    public ResponseMessage<Exam> publishExam(
            @PathVariable Integer examId,
            @RequestBody Map<String, Boolean> data) {
        // 从请求体中获取published字段
        Boolean published = data.get("published");
        if (published == null) {
            published = true; // 默认为发布
        }
        
        Exam exam = examService.publishExam(examId, published);
        String message = published ? "试卷发布成功" : "试卷取消发布成功";
        return ResponseMessage.success(exam, message);
    }

    /**
     * 获取所有已发布的试卷（供学生使用）
     */
    @GetMapping("/published")
    public ResponseMessage<List<Exam>> getPublishedExams() {
        List<Exam> exams = examService.getPublishedExams();
        return ResponseMessage.success(exams, "获取已发布试卷列表成功");
    }
} 