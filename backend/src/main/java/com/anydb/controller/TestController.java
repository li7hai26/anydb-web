package com.anydb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
public class TestController {
    
    @GetMapping("/test")
    public String test() {
        return "Test API working!";
    }
    
    @GetMapping("/api/test")  
    public String apiTest() {
        return "API Test working!";
    }
}