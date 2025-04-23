package com.example.houduan.controller;

import com.example.houduan.pojo.ExamStatistics;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.ScoreDistribution;
import com.example.houduan.service.IExamStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam-statistics")
@CrossOrigin
public class ExamStatisticsController {

    @Autowired
    private IExamStatisticsService examStatisticsService;

    /**
     * 获取所有考试统计数据
     */
    @GetMapping
    public ResponseMessage<List<ExamStatistics>> getAllStatistics() {
        List<ExamStatistics> statistics = examStatisticsService.getAllStatistics();
        if (statistics.isEmpty()) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), "未找到考试统计数据", "暂无任何考试统计信息");
        }
        return ResponseMessage.success(statistics, "获取所有考试统计数据成功");
    }

    /**
     * 获取特定考试的统计数据
     */
    @GetMapping("/exam/{examId}")
    public ResponseMessage<ExamStatistics> getStatisticsByExamId(@PathVariable Integer examId) {
        try {
            ExamStatistics statistics = examStatisticsService.getStatisticsByExamId(examId);
            return ResponseMessage.success(statistics, "获取考试统计数据成功");
        } catch (Exception e) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), "未找到考试统计数据", e.getMessage());
        }
    }
    
    /**
     * 获取考试分数分布
     */
    @GetMapping("/score-distribution/{examId}")
    public ResponseMessage<ScoreDistribution> getScoreDistribution(@PathVariable Integer examId) {
        try {
            ScoreDistribution distribution = examStatisticsService.getScoreDistribution(examId);
            return ResponseMessage.success(distribution, "获取考试分数分布成功");
        } catch (Exception e) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), "获取考试分数分布失败", e.getMessage());
        }
    }

    /**
     * 更新考试统计数据
     */
    @PostMapping("/update/{examId}")
    public ResponseMessage<ExamStatistics> updateStatistics(@PathVariable Integer examId) {
        try {
            ExamStatistics statistics = examStatisticsService.updateStatistics(examId);
            return ResponseMessage.success(statistics, "更新考试统计数据成功");
        } catch (Exception e) {
            return ResponseMessage.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新考试统计数据失败", e.getMessage());
        }
    }

    /**
     * 删除考试统计数据
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteStatistics(@PathVariable Long id) {
        try {
            examStatisticsService.deleteStatistics(id);
            return ResponseMessage.success(null, "删除考试统计数据成功");
        } catch (Exception e) {
            return ResponseMessage.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "删除考试统计数据失败", e.getMessage());
        }
    }
} 