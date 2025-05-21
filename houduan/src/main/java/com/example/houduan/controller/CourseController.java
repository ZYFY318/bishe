package com.example.houduan.controller;

import com.example.houduan.pojo.Course;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.dto.CourseDto;
import com.example.houduan.service.ICourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/courses")
@CrossOrigin
public class CourseController {

    @Autowired
    private ICourseService courseService;

    /**
     * 获取所有课程
     */
    @GetMapping
    public ResponseMessage<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseMessage.success(courses, "获取课程列表成功");
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public ResponseMessage<Course> getCourseById(@PathVariable Integer id) {
        Course course = courseService.getCourseById(id);
        return ResponseMessage.success(course, "获取课程详情成功");
    }

    /**
     * 创建课程
     */
    @PostMapping("/create")
    public ResponseMessage<Course> createCourse(
            @RequestParam("title") String title,
            @RequestParam("duration") Integer duration,
            @RequestParam("creatorId") Integer creatorId,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        
        CourseDto courseDto = new CourseDto();
        courseDto.setTitle(title);
        courseDto.setDuration(duration);
        courseDto.setCreatorId(creatorId);
        
        Course newCourse = courseService.createCourse(courseDto, cover);
        return ResponseMessage.success(newCourse, "课程创建成功");
    }

    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    public ResponseMessage<Course> updateCourse(
            @PathVariable Integer id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "creatorId", required = false) Integer creatorId,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        
        CourseDto courseDto = new CourseDto();
        courseDto.setTitle(title);
        courseDto.setDuration(duration);
        courseDto.setCreatorId(creatorId);
        
        Course updatedCourse = courseService.updateCourse(id, courseDto, cover);
        return ResponseMessage.success(updatedCourse, "课程更新成功");
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteCourse(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseMessage.success(null, "课程删除成功");
    }
}