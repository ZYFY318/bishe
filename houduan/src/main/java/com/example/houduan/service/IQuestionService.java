package com.example.houduan.service;


import com.example.houduan.pojo.Question;
import com.example.houduan.pojo.dto.QuestionDto;

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
}
