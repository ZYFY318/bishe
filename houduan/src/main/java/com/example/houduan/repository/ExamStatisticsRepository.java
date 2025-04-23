package com.example.houduan.repository;

import com.example.houduan.pojo.ExamStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamStatisticsRepository extends JpaRepository<ExamStatistics, Long> {
    /**
     * 根据考试ID查询统计数据
     */
    Optional<ExamStatistics> findByExamId(Integer examId);
} 