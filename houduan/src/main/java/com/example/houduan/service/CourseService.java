package com.example.houduan.service;

import com.example.houduan.exception.ResourceNotFoundException;
import com.example.houduan.pojo.Course;
import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.CourseDto;
import com.example.houduan.repository.CourseRepository;
import com.example.houduan.repository.UserRepository;
import com.example.houduan.config.FileStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService implements ICourseService {

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;  // 新增用户仓库依赖

    @Autowired
    private FileStorageConfig fileStorageConfig; // 新增文件存储配置依赖

    @Override
    public List<Course> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        fillCreatorNames(courses);  // 新增：填充创建者名称
        return courses;
    }

    @Override
    public Course getCourseById(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + id));
        fillCreatorName(course);  // 新增：填充创建者名称
        return course;
    }

    @Override
    public Course createCourse(CourseDto courseDto, MultipartFile cover) {
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDuration(courseDto.getDuration());
        course.setCreatorId(courseDto.getCreatorId());

        // 处理封面图片
        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageConfig.storeFile(cover);
            course.setCoverUrl(coverUrl);
        }
        
        Course savedCourse = courseRepository.save(course);
        fillCreatorName(savedCourse);  // 新增：填充创建者名称
        return savedCourse;
    }

    @Override
    @Transactional
    public Course updateCourse(Integer id, CourseDto courseDto, MultipartFile cover) {
        Course existingCourse = getCourseById(id);
        if (courseDto.getTitle() != null) {
            existingCourse.setTitle(courseDto.getTitle());
        }
        if (courseDto.getDuration() != null) {
            existingCourse.setDuration(courseDto.getDuration());
        }
        if (courseDto.getCreatorId() != null) {
            existingCourse.setCreatorId(courseDto.getCreatorId());
        }


        // 更新封面图片
        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageConfig.storeFile(cover);
            existingCourse.setCoverUrl(coverUrl);
        }
        
        Course updatedCourse = courseRepository.save(existingCourse);
        fillCreatorName(updatedCourse);  // 新增：填充创建者名称
        return updatedCourse;
    }

    @Override
    @Transactional
    public void deleteCourse(Integer id) {
        Course course = getCourseById(id);
        courseRepository.delete(course);
    }

    /**
     * 填充单个课程的创建者名称
     */
    private void fillCreatorName(Course course) {
        if (course != null && course.getCreatorId() != null) {
            Optional<User> userOpt = userRepository.findById(course.getCreatorId());
            userOpt.ifPresent(user -> course.setCreatorName(user.getUsername()));
        }
    }

    /**
     * 批量填充课程列表的创建者名称
     */
    private void fillCreatorNames(List<Course> courses) {
        if (courses != null && !courses.isEmpty()) {
            // 获取所有创建者ID
            List<Integer> creatorIds = courses.stream()
                    .map(Course::getCreatorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询所有创建者
            Iterable<User> users = userRepository.findAllById(creatorIds);

            // 构建创建者ID到用户名的映射
            Map<Integer, String> creatorMap = new HashMap<>();
            users.forEach(user -> creatorMap.put(user.getUserId(), user.getUsername()));

            // 设置创建者名称
            courses.forEach(course -> {
                if (course.getCreatorId() != null && creatorMap.containsKey(course.getCreatorId())) {
                    course.setCreatorName(creatorMap.get(course.getCreatorId()));
                }
            });
        }
    }
}