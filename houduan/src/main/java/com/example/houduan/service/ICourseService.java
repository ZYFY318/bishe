package com.example.houduan.service;

import com.example.houduan.pojo.Course;
import com.example.houduan.pojo.dto.CourseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICourseService {
    List<Course> getAllCourses();
    Course getCourseById(Integer id);
    Course createCourse(CourseDto courseDto, MultipartFile cover);
    Course updateCourse(Integer id, CourseDto courseDto, MultipartFile cover);
    void deleteCourse(Integer id);
Course updateCourseContent(Integer id, CourseDto courseDto);

/**
 * 发布或取消发布课程
 * @param courseId 课程ID
 * @param published 是否发布
 * @return 操作后的课程对象
 */
Course publishCourse(Integer courseId, Boolean published);
}