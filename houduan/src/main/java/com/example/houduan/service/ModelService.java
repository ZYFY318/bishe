package com.example.houduan.service;

import com.example.houduan.exception.ResourceNotFoundException;
import com.example.houduan.pojo.Model;
import com.example.houduan.pojo.dto.ModelDto;
import com.example.houduan.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelService implements IModelService {

    @Autowired
    private ModelRepository modelRepository;

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
    public Model addModel(ModelDto modelDto) {
        Model model = new Model();
        model.setName(modelDto.getName());
        model.setImageUrl(modelDto.getImageUrl());
        model.setDescription(modelDto.getDescription());
        model.setGlbPath(modelDto.getGlbPath());
        
        return modelRepository.save(model);
    }

    @Override
    public Model updateModel(ModelDto modelDto) {
        // Check if model exists
        Model existingModel = getModelById(modelDto.getId());
        
        // Update model fields
        existingModel.setName(modelDto.getName());
        existingModel.setImageUrl(modelDto.getImageUrl());
        existingModel.setDescription(modelDto.getDescription());
        existingModel.setGlbPath(modelDto.getGlbPath());
        
        return modelRepository.save(existingModel);
    }

    @Override
    public void deleteModel(Integer id) {
        // Check if model exists
        Model model = getModelById(id);
        modelRepository.delete(model);
    }
} 