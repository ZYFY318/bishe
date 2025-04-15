package com.example.houduan.service;

import com.example.houduan.config.FileStorageConfig;
import com.example.houduan.exception.ResourceNotFoundException;
import com.example.houduan.pojo.Model;
import com.example.houduan.pojo.User;
import com.example.houduan.pojo.dto.ModelDto;
import com.example.houduan.repository.ModelRepository;
import com.example.houduan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ModelService implements IModelService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Override
    public List<Model> getAllModels() {
        return modelRepository.findAll();
    }

    @Override
    public Model getModelById(Integer id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));
    }

    @Override
    public Model addModel(ModelDto modelDto, byte[] glbData, MultipartFile cover) {
        Model model = new Model();
        model.setName(modelDto.getName());
        model.setDescription(modelDto.getDescription());
        model.setFileName(modelDto.getFileName());
        model.setGlbData(glbData);
        
        // 处理封面图片
        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageConfig.storeFile(cover);
            model.setCoverUrl(coverUrl);
        }
        
        // 设置创建者ID
        if (modelDto.getCreatorId() != null) {
            model.setCreatorId(modelDto.getCreatorId());
            
            // 根据创建者ID获取用户名
            try {
                User creator = userRepository.findById(modelDto.getCreatorId()).orElse(null);
                if (creator != null) {
                    model.setCreatorName(creator.getUsername());
                }
            } catch (Exception e) {
                // 如果获取用户失败，不阻止模型保存
                System.out.println("Failed to get creator info: " + e.getMessage());
            }
        }
        
        return modelRepository.save(model);
    }

    @Override
    public Model updateModel(ModelDto modelDto, byte[] glbData, MultipartFile cover) {
        // Check if model exists
        Model existingModel = getModelById(modelDto.getId());
        
        // Update model fields
        existingModel.setName(modelDto.getName());
        existingModel.setDescription(modelDto.getDescription());
        
        // 更新封面图片
        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageConfig.storeFile(cover);
            existingModel.setCoverUrl(coverUrl);
        }
        
        // Update file data only if new file is provided
        if (glbData != null) {
            existingModel.setFileName(modelDto.getFileName());
            existingModel.setGlbData(glbData);
        }
        
        // 更新创建者ID (如果提供)
        if (modelDto.getCreatorId() != null) {
            existingModel.setCreatorId(modelDto.getCreatorId());
            
            // 根据创建者ID获取用户名
            try {
                User creator = userRepository.findById(modelDto.getCreatorId()).orElse(null);
                if (creator != null) {
                    existingModel.setCreatorName(creator.getUsername());
                }
            } catch (Exception e) {
                // 如果获取用户失败，不阻止模型保存
                System.out.println("Failed to get creator info: " + e.getMessage());
            }
        }
        
        return modelRepository.save(existingModel);
    }

    @Override
    public void deleteModel(Integer id) {
        // Check if model exists
        Model model = getModelById(id);
        modelRepository.delete(model);
    }

    @Override
    public List<Model> getModelsByCreatorId(Integer creatorId) {
        return modelRepository.findByCreatorId(creatorId);
    }
} 