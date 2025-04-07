package com.example.houduan.service;

import com.example.houduan.config.FileStorageConfig;
import com.example.houduan.exception.ResourceNotFoundException;
import com.example.houduan.pojo.Exam;
import com.example.houduan.pojo.ExamQuestion;
import com.example.houduan.pojo.Question;
import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.ExamDto;
import com.example.houduan.pojo.dto.ExamQuestionsDto;
import com.example.houduan.pojo.dto.ExamQuestionsResponseDto;
import com.example.houduan.repository.ExamQuestionRepository;
import com.example.houduan.repository.ExamRepository;
import com.example.houduan.repository.QuestionRepository;
import com.example.houduan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExamService implements IExamService {

    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Override
    public List<Exam> getAllExams() {
        List<Exam> exams = examRepository.findAll();
        // 为每个试卷填充创建者名称
        fillCreatorNames(exams);
        return exams;
    }

    @Override
    public Exam getExamById(Integer id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在，ID: " + id));
        // 填充创建者名称
        fillCreatorName(exam);
        return exam;
    }

    @Override
    public List<Exam> getExamsByCreatorId(Integer creatorId) {
        List<Exam> exams = examRepository.findByCreatorId(creatorId);
        // 填充创建者名称
        fillCreatorNames(exams);
        return exams;
    }

    @Override
    public List<Exam> getPublishedExams() {
        List<Exam> exams = examRepository.findByPublishedTrue();
        // 填充创建者名称
        fillCreatorNames(exams);
        return exams;
    }

    @Override
    public Exam createExam(ExamDto examDto, MultipartFile cover) {
        Exam exam = new Exam();
        exam.setTitle(examDto.getTitle());
        exam.setDuration(examDto.getDuration());
        exam.setCreatorId(examDto.getCreatorId());
        
        // 处理封面图片
        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageConfig.storeFile(cover);
            exam.setCoverUrl(coverUrl);
        }
        
        Exam savedExam = examRepository.save(exam);
        // 填充创建者名称
        fillCreatorName(savedExam);
        return savedExam;
    }

    @Override
    public Exam updateExam(Integer id, ExamDto examDto, MultipartFile cover) {
        Exam exam = getExamById(id);
        
        // 更新标题
        if (examDto.getTitle() != null) {
            exam.setTitle(examDto.getTitle());
        }
        
        // 更新考试时长
        if (examDto.getDuration() != null) {
            exam.setDuration(examDto.getDuration());
        }
        
        // 更新封面图片
        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageConfig.storeFile(cover);
            exam.setCoverUrl(coverUrl);
        }
        
        Exam updatedExam = examRepository.save(exam);
        // 填充创建者名称
        fillCreatorName(updatedExam);
        return updatedExam;
    }

    @Override
    @Transactional
    public void deleteExam(Integer id) {
        Exam exam = getExamById(id);
        
        // 删除试卷与题目的关联
        examQuestionRepository.deleteByExamId(id);
        
        // 删除试卷
        examRepository.delete(exam);
    }
    
    @Override
    public ExamQuestionsResponseDto getExamQuestions(Integer examId) {
        // 验证试卷是否存在
        Exam exam = getExamById(examId);
        
        // 获取试卷关联的题目ID
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamId(examId);
        
        // 如果没有关联题目，返回空列表
        if (examQuestions.isEmpty()) {
            ExamQuestionsResponseDto responseDto = new ExamQuestionsResponseDto();
            responseDto.setQuestions(new ArrayList<>());
            responseDto.setTotal(0);
            return responseDto;
        }
        
        // 提取题目ID列表
        List<Long> questionIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .collect(Collectors.toList());
        
        // 查询题目详情
        List<Question> questions = questionRepository.findAllById(questionIds);
        
        // 构建响应对象
        ExamQuestionsResponseDto responseDto = new ExamQuestionsResponseDto();
        responseDto.setQuestions(questions);
        responseDto.setTotal(questions.size());
        
        return responseDto;
    }
    
    @Override
    @Transactional
    public Exam addQuestionsToExam(Integer examId, ExamQuestionsDto examQuestionsDto) {
        // 验证试卷是否存在
        Exam exam = getExamById(examId);
        
        // 清除现有关联
        examQuestionRepository.deleteByExamId(examId);
        
        // 验证题目是否存在并添加关联
        if (examQuestionsDto.getQuestionIds() != null && !examQuestionsDto.getQuestionIds().isEmpty()) {
            for (Long questionId : examQuestionsDto.getQuestionIds()) {
                // 检查题目是否存在
                if (questionRepository.existsById(questionId)) {
                    // 创建新的关联
                    ExamQuestion examQuestion = new ExamQuestion();
                    examQuestion.setExamId(examId);
                    examQuestion.setQuestionId(questionId);
                    examQuestionRepository.save(examQuestion);
                }
            }
        }
        
        return exam;
    }
    
    @Override
    @Transactional
    public void removeQuestionFromExam(Integer examId, Long questionId) {
        // 验证试卷是否存在
        getExamById(examId);
        
        // 验证题目是否存在于试卷中
        boolean exists = examQuestionRepository.existsByExamIdAndQuestionId(examId, questionId);
        if (!exists) {
            throw new ResourceNotFoundException("题目不存在于该试卷中，试卷ID: " + examId + "，题目ID: " + questionId);
        }
        
        // 删除关联
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamId(examId);
        for (ExamQuestion eq : examQuestions) {
            if (eq.getQuestionId().equals(questionId)) {
                examQuestionRepository.delete(eq);
                break;
            }
        }
    }
    
    @Override
    @Transactional
    public Exam publishExam(Integer examId, Boolean published) {
        // 验证试卷是否存在
        Exam exam = getExamById(examId);
        
        // 设置发布状态
        exam.setPublished(published);
        
        // 保存更新
        Exam updatedExam = examRepository.save(exam);
        // 填充创建者名称
        fillCreatorName(updatedExam);
        return updatedExam;
    }
    
    /**
     * 填充单个试卷的创建者名称
     */
    private void fillCreatorName(Exam exam) {
        if (exam != null && exam.getCreatorId() != null) {
            Optional<User> userOpt = userRepository.findById(exam.getCreatorId());
            userOpt.ifPresent(user -> exam.setCreatorName(user.getUsername()));
        }
    }
    
    /**
     * 批量填充试卷列表的创建者名称
     */
    private void fillCreatorNames(List<Exam> exams) {
        if (exams != null && !exams.isEmpty()) {
            // 获取所有创建者ID
            List<Integer> creatorIds = exams.stream()
                    .map(Exam::getCreatorId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 查询所有创建者
            Iterable<User> users = userRepository.findAllById(creatorIds);
            
            // 构建创建者ID到用户名的映射
            java.util.Map<Integer, String> creatorMap = new java.util.HashMap<>();
            users.forEach(user -> creatorMap.put(user.getUserId(), user.getUsername()));
            
            // 设置创建者名称
            exams.forEach(exam -> {
                if (exam.getCreatorId() != null && creatorMap.containsKey(exam.getCreatorId())) {
                    exam.setCreatorName(creatorMap.get(exam.getCreatorId()));
                }
            });
        }
    }
} 