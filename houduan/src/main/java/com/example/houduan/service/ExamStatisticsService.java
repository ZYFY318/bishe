package com.example.houduan.service;

import com.example.houduan.exception.ResourceNotFoundException;
import com.example.houduan.pojo.ExamResult;
import com.example.houduan.pojo.ExamStatistics;
import com.example.houduan.pojo.ScoreDistribution;
import com.example.houduan.repository.ExamResultRepository;
import com.example.houduan.repository.ExamStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ExamStatisticsService implements IExamStatisticsService {

    @Autowired
    private ExamStatisticsRepository examStatisticsRepository;
    
    @Autowired
    private ExamResultRepository examResultRepository;

    @Override
    public ExamStatistics getStatisticsByExamId(Integer examId) {
        return examStatisticsRepository.findByExamId(examId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到考试统计数据，考试ID：" + examId));
    }

    @Override
    public List<ExamStatistics> getAllStatistics() {
        return examStatisticsRepository.findAll();
    }
    
    @Override
    public ScoreDistribution getScoreDistribution(Integer examId) {
        // 创建分数分布对象
        ScoreDistribution distribution = new ScoreDistribution();
        distribution.setExamId(examId);
        
        // 获取该考试的所有成绩
        List<ExamResult> results = examResultRepository.findByExamId(examId);
        
        if (results.isEmpty()) {
            return distribution; // 如果没有成绩，返回空的分布
        }
        
        // 设置总参与人数
        distribution.setTotalParticipants(results.size());
        
        // 获取分数段
        List<Integer> sections = distribution.getSections();
        List<Integer> counts = distribution.getCounts();
        List<Double> percentages = distribution.getPercentages();
        
        // 统计每个分数段的人数
        for (ExamResult result : results) {
            int score = result.getScore();
            for (int i = 0; i < sections.size() - 1; i++) {
                if (score >= sections.get(i) && score < sections.get(i + 1)) {
                    counts.set(i, counts.get(i) + 1);
                    break;
                }
            }
            // 处理满分情况
            if (score == 100) {
                counts.set(counts.size() - 2, counts.get(counts.size() - 2) + 1);
            }
        }
        
        // 计算每个分数段的百分比
        for (int i = 0; i < counts.size(); i++) {
            double percentage = (double) counts.get(i) / results.size() * 100;
            percentages.set(i, Math.round(percentage * 100) / 100.0); // 保留两位小数
        }
        
        return distribution;
    }

    @Override
    @Transactional
    public ExamStatistics updateStatistics(Integer examId) {
        // 查找或创建统计记录
        ExamStatistics statistics = examStatisticsRepository.findByExamId(examId)
                .orElse(new ExamStatistics());
        
        // 设置考试ID
        statistics.setExamId(examId);
        
        // 获取该考试的所有成绩记录
        List<ExamResult> results = examResultRepository.findByExamId(examId);
        
        if (!results.isEmpty()) {
            // 参与人数
            statistics.setParticipantCount(results.size());
            
            // 计算平均分
            double totalScore = results.stream()
                    .mapToDouble(ExamResult::getScore)
                    .sum();
            statistics.setAvgScore(totalScore / results.size());
            
            // 计算及格人数
            long passingCount = results.stream()
                    .filter(result -> result.getScore() >= statistics.getPassingScore())
                    .count();
            statistics.setPassingCount((int) passingCount);
            
            // 计算最高分
            int highestScore = results.stream()
                    .mapToInt(ExamResult::getScore)
                    .max()
                    .orElse(0);
            statistics.setHighestScore(highestScore);
            
            // 计算最低分
            int lowestScore = results.stream()
                    .mapToInt(ExamResult::getScore)
                    .min()
                    .orElse(0);
            statistics.setLowestScore(lowestScore);
            
            // 计算总用时和平均用时
            long totalTime = results.stream()
                    .mapToLong(ExamResult::getDuration)  // 使用duration来获取用时
                    .sum();
            statistics.setTotalTime(totalTime);
            statistics.setAvgTime((double) totalTime / results.size());
        }
        
        // 保存并返回更新后的统计数据
        return examStatisticsRepository.save(statistics);
    }

    @Override
    @Transactional
    public void deleteStatistics(Long id) {
        examStatisticsRepository.deleteById(id);
    }
} 