package com.example.houduan.controller;

import com.example.houduan.pojo.Exam;
import com.example.houduan.service.IExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExamControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IExamService examService;

    @InjectMocks
    private ExamController examController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(examController).build();
    }

    @Test
    public void testGetAllExams() throws Exception {
        // 准备测试数据
        Exam exam1 = new Exam();
        exam1.setId(1);
        exam1.setTitle("期中考试");
        exam1.setCoverUrl("/uploads/covers/1.jpg");
        exam1.setDuration(60);
        exam1.setCreatedAt(LocalDateTime.now());

        Exam exam2 = new Exam();
        exam2.setId(2);
        exam2.setTitle("期末考试");
        exam2.setCoverUrl("/uploads/covers/2.jpg");
        exam2.setDuration(120);
        exam2.setCreatedAt(LocalDateTime.now());

        List<Exam> exams = Arrays.asList(exam1, exam2);

        // 配置模拟行为
        when(examService.getAllExams()).thenReturn(exams);

        // 执行请求并验证响应
        mockMvc.perform(get("/api/exams")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("获取试卷列表成功")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].title", is("期中考试")))
                .andExpect(jsonPath("$.data[0].duration", is(60)))
                .andExpect(jsonPath("$.data[1].id", is(2)))
                .andExpect(jsonPath("$.data[1].title", is("期末考试")))
                .andExpect(jsonPath("$.data[1].duration", is(120)));
    }

    @Test
    public void testCreateExam() throws Exception {
        // 准备测试数据
        Exam newExam = new Exam();
        newExam.setId(3);
        newExam.setTitle("模拟考试");
        newExam.setCoverUrl("/uploads/covers/3.jpg");
        newExam.setDuration(90);
        newExam.setCreatedAt(LocalDateTime.now());

        // 配置模拟行为
        when(examService.createExam(any(), any())).thenReturn(newExam);

        // 创建模拟的文件上传
        MockMultipartFile coverFile = new MockMultipartFile(
                "cover", 
                "test-cover.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes());

        // 执行请求并验证响应
        mockMvc.perform(multipart("/api/exams/create")
                .file(coverFile)
                .param("title", "模拟考试")
                .param("duration", "90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("试卷创建成功")))
                .andExpect(jsonPath("$.data.id", is(3)))
                .andExpect(jsonPath("$.data.title", is("模拟考试")))
                .andExpect(jsonPath("$.data.coverUrl", is("/uploads/covers/3.jpg")))
                .andExpect(jsonPath("$.data.duration", is(90)));
    }
} 