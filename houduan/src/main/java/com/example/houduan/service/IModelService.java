package com.example.houduan.service;

import com.example.houduan.pojo.Model;
import com.example.houduan.pojo.dto.ModelDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IModelService {
    // Get all models
    List<Model> getAllModels();
    
    
    // Get model by id
    Model getModelById(Integer id);
    
    // Get models by creator id
    List<Model> getModelsByCreatorId(Integer creatorId);
    
    // Add a new model
    Model addModel(ModelDto modelDto, byte[] glbData, MultipartFile cover);
    
    // Update a model
    Model updateModel(ModelDto modelDto, byte[] glbData, MultipartFile cover);
    
    // Delete a model
    void deleteModel(Integer id);
} 