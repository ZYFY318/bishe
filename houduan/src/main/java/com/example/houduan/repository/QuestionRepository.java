package com.example.houduan.repository;

import com.example.houduan.pojo.Question;
//import org.hibernate.query.Page;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//import java.awt.print.Pageable;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM tb_question ORDER BY RAND() LIMIT :num", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("num") int num);

    // 根据题目标题包含关键词查询
    @Query("SELECT q FROM Question q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Question> findByTitleContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

}
