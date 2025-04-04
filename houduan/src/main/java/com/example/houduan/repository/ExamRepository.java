package com.example.houduan.repository;

import com.example.houduan.pojo.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Integer> {
    // 基本CRUD操作由JpaRepository提供
} 