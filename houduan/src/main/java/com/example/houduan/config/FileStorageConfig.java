package com.example.houduan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Bean
    public Path fileStoragePath() {
        Path fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            // 确保目录存在
            if (!Files.exists(fileStoragePath)) {
                Files.createDirectories(fileStoragePath);
            }
            
            // 创建covers子目录
            Path coversPath = fileStoragePath.resolve("covers");
            if (!Files.exists(coversPath)) {
                Files.createDirectories(coversPath);
            }
            
            // 创建avatars子目录
            Path avatarsPath = fileStoragePath.resolve("avatars");
            if (!Files.exists(avatarsPath)) {
                Files.createDirectories(avatarsPath);
            }
            
            return fileStoragePath;
        } catch (IOException ex) {
            throw new RuntimeException("无法创建文件上传目录", ex);
        }
    }

    /**
     * 保存上传的文件
     */
    public String storeFile(MultipartFile file) {
        return storeFile(file, "covers"); // 默认存储到covers目录，保持向后兼容
    }

    /**
     * 保存上传的文件到指定子目录
     */
    public String storeFile(MultipartFile file, String subDirectory) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                throw new RuntimeException("无法保存空文件");
            }
            
            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + extension;
            Path targetPath = fileStoragePath().resolve(subDirectory).resolve(fileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回相对路径
            return "/uploads/" + subDirectory + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("无法存储文件", ex);
        }
    }

    /**
     * 加载文件作为资源
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = fileStoragePath().resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if(resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("文件不存在: " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("文件不存在", ex);
        }
    }

    /**
     * 配置静态资源访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + fileStoragePath().toFile().getAbsolutePath() + "/");
    }
} 