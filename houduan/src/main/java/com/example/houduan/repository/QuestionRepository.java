package com.example.houduan.repository;

import com.example.houduan.pojo.Question;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM tb_question ORDER BY RAND() LIMIT :num", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("num") int num);

}
