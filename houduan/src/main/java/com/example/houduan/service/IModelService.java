package com.example.houduan.service;

import com.example.houduan.pojo.Model;
import com.example.houduan.pojo.dto.ModelDto;

import java.util.List;

public interface IModelService {
    // Get all models
    List<Model> getAllModels();
    
    // Get model by id
    Model getModelById(Integer id);
    
    // Add a new model
    Model addModel(ModelDto modelDto);
    
    // Update a model
    Model updateModel(ModelDto modelDto);
    
    // Delete a model
    void deleteModel(Integer id);
} 