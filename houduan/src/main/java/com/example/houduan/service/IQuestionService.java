package com.example.houduan.service;


import com.example.houduan.pojo.Question;
import com.example.houduan.pojo.dto.QuestionDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IQuestionService {

    // 获取所有题目
    List<Question> getAllQuestions();

    // 添加题目
    Question addQuestion(QuestionDto questionDto);

    // 删除题目
    void deleteQuestion(Long id);

    // 更新题目
    Question updateQuestion(Long id, QuestionDto questionDto);

    Page<Question> getQuestionsByPage(Pageable pageable);

    List<Question> getRandomQuestions(int num);
    
    // 根据关键词搜索题目
    Page<Question> searchQuestions(String keyword, Pageable pageable);
}
