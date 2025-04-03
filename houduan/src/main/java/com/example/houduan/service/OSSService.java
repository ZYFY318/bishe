package com.example.houduan.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class OSSService {
    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    /**
     * 上传GLB文件到OSS
     * @param file GLB文件
     * @return 文件访问URL
     */
    public String uploadGlbFile(MultipartFile file) throws IOException {
        // 生成唯一的文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = "models/" + UUID.randomUUID().toString() + extension;

        // 设置文件元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("model/gltf-binary"); // GLB文件的MIME类型
        metadata.setContentLength(file.getSize());

        // 上传文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata);
        ossClient.putObject(putObjectRequest);

        // 返回文件访问URL
        return "https://" + bucketName + "." + endpoint + "/" + fileName;
    }

    /**
     * 删除OSS中的文件
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        // 从URL中提取文件名
        String fileName = fileUrl.substring(fileUrl.indexOf(bucketName + ".") + bucketName.length() + endpoint.length() + 3);
        ossClient.deleteObject(bucketName, fileName);
    }
} 