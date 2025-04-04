package com.example.houduan.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@CrossOrigin
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 处理对上传文件的请求
     * 
     * @param folder 文件夹名称 (例如 "covers")
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/uploads/{folder}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String folder, @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(folder).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.IMAGE_JPEG) // 可根据文件类型动态设置
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 