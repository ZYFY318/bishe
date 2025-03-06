package com.example.houduan.repository;

import com.example.houduan.pojo.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
