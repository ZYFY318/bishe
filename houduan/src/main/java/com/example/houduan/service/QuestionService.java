package com.example.houduan.service;

import com.example.houduan.pojo.Question;
import com.example.houduan.pojo.dto.QuestionDto;
import com.example.houduan.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService implements IQuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    // 获取所有题目
    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    // 添加题目
    @Override
    public Question addQuestion(QuestionDto questionDto) {
        // 将 QuestionDto 转换成 Question 实体类
        Question question = new Question();
        BeanUtils.copyProperties(questionDto, question);
        // 保存并返回保存后的实体
        return questionRepository.save(question);
    }

    // 删除题目
    @Override
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    // 更新题目
    @Override
    public Question updateQuestion(Long id, QuestionDto questionDto) {
        // 根据 id 查找题目
        Optional<Question> existingQuestion = questionRepository.findById(id);
        if (existingQuestion.isPresent()) {
            Question question = existingQuestion.get();
            // 使用 BeanUtils 复制属性
            BeanUtils.copyProperties(questionDto, question);
            // 保存更新后的题目
            return questionRepository.save(question);
        }
        return null;  // 如果没有找到该题目，则返回 null
    }

}