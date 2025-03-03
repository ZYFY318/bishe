package com.example.houduan.repository;

import com.example.houduan.pojo.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByUsername(String username);
}
