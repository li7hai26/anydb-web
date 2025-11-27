package com.anydb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 主页控制器
 */
@Controller
public class HomeController {
    
    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "AnyDB Backend is running!";
    }
    
    @RequestMapping("/api")
    @ResponseBody
    public String api() {
        return "API working!";
    }
}