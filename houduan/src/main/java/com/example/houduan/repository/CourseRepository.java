package com.example.houduan.repository;

import com.example.houduan.pojo.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    // 根据创建者ID查询课程列表
    List<Course> findByCreatorId(Integer creatorId);

    // 查询所有已发布的课程
    List<Course> findByIsPublishedTrue();
}