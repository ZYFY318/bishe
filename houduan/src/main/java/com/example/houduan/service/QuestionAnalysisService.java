package com.example.houduan.service;

import com.example.houduan.exception.ResourceNotFoundException;
import com.example.houduan.pojo.ExamQuestion;
import com.example.houduan.pojo.ExamResult;
import com.example.houduan.pojo.Question;
import com.example.houduan.pojo.QuestionAnalysis;
import com.example.houduan.repository.ExamQuestionRepository;
import com.example.houduan.repository.ExamResultRepository;
import com.example.houduan.repository.QuestionAnalysisRepository;
import com.example.houduan.repository.QuestionRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionAnalysisService implements IQuestionAnalysisService {

    @Autowired
    private QuestionAnalysisRepository questionAnalysisRepository;
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public List<QuestionAnalysis> getAnalysisByExamId(Integer examId) {
        return questionAnalysisRepository.findByExamId(examId);
    }

    @Override
    public QuestionAnalysis getAnalysisByQuestionId(Integer examId, Long questionId) {
        return questionAnalysisRepository.findByExamIdAndQuestionId(examId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到题目分析数据，考试ID: " + examId + ", 题目ID: " + questionId));
    }

    @Override
    @Transactional
    public QuestionAnalysis updateQuestionAnalysis(QuestionAnalysis analysis) {
        return questionAnalysisRepository.save(analysis);
    }

    @Override
    @Transactional
    public List<QuestionAnalysis> updateExamQuestionAnalysis(Integer examId) {
        // 获取考试中的所有题目
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamId(examId);
        if (examQuestions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取考试的所有结果
        List<ExamResult> examResults = examResultRepository.findByExamId(examId);
        if (examResults.isEmpty()) {
            System.out.println("here");
            return new ArrayList<>();
        }
        System.out.println(examResults);
        // 更新每个题目的分析
        List<QuestionAnalysis> analyses = new ArrayList<>();
        for (ExamQuestion eq : examQuestions) {
            long questionId = eq.getQuestionId();
            
            // 查找现有分析或创建新分析
            QuestionAnalysis analysis = questionAnalysisRepository
                    .findByExamIdAndQuestionId(examId, questionId)
                    .orElse(new QuestionAnalysis());
            
            // 设置基本信息
            analysis.setExamId(examId);
            analysis.setQuestionId(questionId);
            
            // 获取题目信息
            Optional<Question> questionOpt = questionRepository.findById(questionId);
            if (questionOpt.isPresent()) {
                Question question = questionOpt.get();
                // 设置题目描述摘要（取前30个字符）
                String desc = question.getTitle();
                if (desc.length() > 30) {
                    desc = desc.substring(0, 30) + "...";
                }
                analysis.setDescription(desc);
            }
            
            // 计算答题情况
            int attemptCount = 0;
            int correctCount = 0;
            
            for (ExamResult result : examResults) {
                String userAnswers = result.getUserAnswers();
                if (userAnswers != null && !userAnswers.isEmpty()) {
                    try {
                        JSONObject answersObject = new JSONObject(userAnswers);
                        for (String questionIdStr : answersObject.keySet()) {
                            int selectedOption = answersObject.getInt(questionIdStr);
                            if (questionIdStr.equals(String.valueOf(questionId))) {
                                attemptCount++;
                                boolean isCorrect = selectedOption == 1; // Assuming 1 is correct
                                if (isCorrect) {
                                    correctCount++;
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // 解析JSON出错，跳过这个结果
                        System.err.println("解析用户答案时出错: " + e.getMessage());
                    }
                }
            }
            
            // 更新统计数据
            analysis.setAttemptCount(attemptCount);
            analysis.setCorrectCount(correctCount);
            
            // 计算正确率
            if (attemptCount > 0) {
                double correctRate = (double) correctCount / attemptCount;
                analysis.setCorrectRate(correctRate);
            } else {
                analysis.setCorrectRate(0.0);
            }
            
            // 保存分析
            analyses.add(questionAnalysisRepository.save(analysis));
        }
        
        return analyses;
    }

    @Override
    @Transactional
    public void deleteAnalysis(Long id) {
        questionAnalysisRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByExamId(Integer examId) {
        questionAnalysisRepository.deleteByExamId(examId);
    }
} 