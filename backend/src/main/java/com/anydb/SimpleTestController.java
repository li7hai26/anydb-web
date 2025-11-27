package com.anydb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简化版测试控制器
 */
@RestController
public class SimpleTestController {
    
    @GetMapping("/simple")
    public String simple() {
        return "Simple test working!";
    }
}