package com.example.houduan.controller;

import com.example.houduan.pojo.Model;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.dto.ModelDto;
import com.example.houduan.service.IModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/model")
@CrossOrigin // 允许跨域请求
public class ModelController {

    @Autowired
    private IModelService modelService;

    /**
     * Get all models (metadata only, without GLB data for better performance)
     */
    @GetMapping
    public ResponseMessage<List<ModelListDto>> getAllModels() {
        List<Model> models = modelService.getAllModels();
        List<ModelListDto> modelListDtos = models.stream()
                .map(model -> new ModelListDto(
                        model.getId(),
                        model.getName(),
                        model.getDescription(),
                        model.getImageUrl(),
                        model.getCreatedAt()))
                .toList();
        return ResponseMessage.success(modelListDtos);
    }

    /**
     * Get model by ID including GLB data
     */
    @GetMapping("/{id}")
    public ResponseMessage<Model> getModelById(@PathVariable Integer id) {
        Model model = modelService.getModelById(id);
        return ResponseMessage.success(model);
    }

    /**
     * Download GLB file
     */
    @GetMapping("/{id}/glb")
    public ResponseEntity<ByteArrayResource> downloadGlb(@PathVariable Integer id) {
        Model model = modelService.getModelById(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("model/gltf-binary"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + model.getFileName() + "\"")
                .body(new ByteArrayResource(model.getGlbData()));
    }

    /**
     * Add a new model with GLB file
     */
    @PostMapping("/upload")
    public ResponseMessage<Model> addModelWithFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) throws IOException {
        
        // 创建ModelDto对象
        ModelDto modelDto = new ModelDto();
        modelDto.setName(name);
        modelDto.setDescription(description);
        modelDto.setImageUrl(imageUrl);
        modelDto.setFileName(file.getOriginalFilename());
        
        // 保存模型信息
        Model newModel = modelService.addModel(modelDto, file.getBytes());
        return ResponseMessage.success(newModel);
    }

    /**
     * Update an existing model
     */
    @PutMapping("/{id}")
    public ResponseMessage<Model> updateModel(
            @PathVariable Integer id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) throws IOException {
        
        // 创建ModelDto对象
        ModelDto modelDto = new ModelDto();
        modelDto.setId(id);
        modelDto.setName(name);
        modelDto.setDescription(description);
        modelDto.setImageUrl(imageUrl);
        
        // 如果有新文件，更新文件名
        if (file != null && !file.isEmpty()) {
            modelDto.setFileName(file.getOriginalFilename());
        }
        
        // 更新模型信息
        Model updatedModel = modelService.updateModel(modelDto, file != null ? file.getBytes() : null);
        return ResponseMessage.success(updatedModel);
    }

    /**
     * Delete a model
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteModel(@PathVariable Integer id) {
        modelService.deleteModel(id);
        return ResponseMessage.success();
    }

    // 用于列表展示的简化DTO
    private static class ModelListDto {
        private Integer id;
        private String name;
        private String description;
        private String imageUrl;
        private String created_at;  // 改为String类型，以便自定义格式化

        public ModelListDto(Integer id, String name, String description, 
                          String imageUrl, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.imageUrl = imageUrl;
            // 格式化时间为ISO 8601格式
            this.created_at = createdAt != null ? 
                createdAt.toString() : null;
        }

        // Getters
        public Integer getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getImageUrl() { return imageUrl; }
        public String getCreated_at() { return created_at; }
    }
} 