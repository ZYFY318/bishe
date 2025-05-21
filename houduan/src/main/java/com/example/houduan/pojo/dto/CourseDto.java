package com.example.houduan.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class CourseDto {
    private Integer id;

    @NotBlank(message = "课程名称不能为空")
    private String title;

    @Min(value = 1, message = "课程时长必须大于 0")
    private Integer duration;

    private String coverUrl;

    private Integer creatorId;


}