package com.example.houduan.repository;

import com.example.houduan.pojo.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamResultRepository extends JpaRepository<ExamResult, Integer> {
    List<ExamResult> findByUserId(Integer userId); // 根据用户 ID 查询考试成绩
}
