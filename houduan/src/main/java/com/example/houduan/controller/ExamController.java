package com.example.houduan.controller;

import com.example.houduan.pojo.Exam;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.dto.ExamDto;
import com.example.houduan.service.IExamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * 创建试卷
     */
    @PostMapping("/create")
    public ResponseMessage<Exam> createExam(
            @RequestParam("title") String title,
            @RequestParam("duration") Integer duration,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        
        ExamDto examDto = new ExamDto();
        examDto.setTitle(title);
        examDto.setDuration(duration);
        
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
} 