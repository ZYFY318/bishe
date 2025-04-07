package com.example.houduan.repository;

import com.example.houduan.pojo.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Integer> {
    // 基本CRUD操作由JpaRepository提供
    
    /**
     * 查询特定创建者的所有试卷
     */
    List<Exam> findByCreatorId(Integer creatorId);

    /**
     * 查询所有已发布的试卷
     */
    List<Exam> findByPublishedTrue();
} 