package com.example.houduan.controller;

import com.example.houduan.pojo.Question;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.dto.QuestionDto;
import com.example.houduan.service.IQuestionService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController //接口方法返回对象转换为json文本
@RequestMapping("/question") //localhost:9090/question/**
public class QuestionController {
    @Autowired
    IQuestionService questionService;

    @GetMapping("/page/{page}/{limit}")
    public ResponseMessage<Page<Question>> getQuestionsByPage(
            @PathVariable int page,
            @PathVariable int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit); // Spring Data Page 是从 0 开始的
        Page<Question> questionPage = questionService.getQuestionsByPage(pageable);
        return ResponseMessage.success(questionPage);
    }
    // 获取所有题目
    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }
    // 添加题目
    @PostMapping
    public ResponseMessage<Question> addQuestion(@RequestBody QuestionDto questionDto) {
        Question savedQuestion = questionService.addQuestion(questionDto);
        return ResponseMessage.success(savedQuestion);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
    // 更新题目
    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Long id, @RequestBody QuestionDto questionDto) {
        Question updatedQuestion = questionService.updateQuestion(id, questionDto);
        if (updatedQuestion != null) {
            return ResponseEntity.ok(updatedQuestion);
        } else {
            return ResponseEntity.notFound().build();  // 如果找不到题目，返回 404
        }
    }
}
