package com.example.houduan.service;

import com.example.houduan.pojo.ExamResult;

import java.util.List;

public interface IExamResultService {
    //根据id获取成绩
    List<ExamResult> getUserExamResults(Integer userId);
    //保存成绩
    ExamResult saveExamResult(ExamResult examResult);
}
