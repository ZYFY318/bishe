package com.example.houduan.repository;

import com.example.houduan.pojo.Question;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
