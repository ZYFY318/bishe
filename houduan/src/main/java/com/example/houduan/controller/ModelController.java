package com.example.houduan.controller;

import com.example.houduan.pojo.Model;
import com.example.houduan.pojo.ResponseMessage;
import com.example.houduan.pojo.dto.ModelDto;
import com.example.houduan.service.IModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/model")
public class ModelController {

    @Autowired
    private IModelService modelService;

    /**
     * Get all models
     * @return ResponseMessage containing a list of all models
     */
    @GetMapping
    public ResponseMessage<List<Model>> getAllModels() {
        List<Model> models = modelService.getAllModels();
        return ResponseMessage.success(models);
    }

    /**
     * Get a specific model by ID
     * @param id the model ID
     * @return ResponseMessage containing the requested model
     */
    @GetMapping("/{id}")
    public ResponseMessage<Model> getModelById(@PathVariable Integer id) {
        Model model = modelService.getModelById(id);
        return ResponseMessage.success(model);
    }

    /**
     * Add a new model
     * @param modelDto the model data
     * @return ResponseMessage containing the created model
     */
    @PostMapping
    public ResponseMessage<Model> addModel(@Validated @RequestBody ModelDto modelDto) {
        Model newModel = modelService.addModel(modelDto);
        return ResponseMessage.success(newModel);
    }

    /**
     * Update an existing model
     * @param modelDto the updated model data
     * @return ResponseMessage containing the updated model
     */
    @PutMapping
    public ResponseMessage<Model> updateModel(@Validated @RequestBody ModelDto modelDto) {
        Model updatedModel = modelService.updateModel(modelDto);
        return ResponseMessage.success(updatedModel);
    }

    /**
     * Delete a model
     * @param id the ID of the model to delete
     * @return ResponseMessage indicating success
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteModel(@PathVariable Integer id) {
        modelService.deleteModel(id);
        return ResponseMessage.success();
    }
} 