package com.anydb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 简化版数据库控制器
 */
@RestController
public class SimpleDatabaseController {
    
    @GetMapping("/api/databases/types")
    public List<String> getSupportedTypes() {
        return Arrays.asList("MySQL", "PostgreSQL", "Redis", "MongoDB", "Elasticsearch");
    }
    
    @GetMapping("/api/databases/test")
    public String test() {
        return "Database API is working!";
    }
}