package com.example.houduan.repository;

import com.example.houduan.pojo.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, Integer> {
    // The basic CRUD operations are provided by JpaRepository
    // Add custom query methods if needed
} 