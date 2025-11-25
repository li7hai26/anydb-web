package com.anydb.controller;

import com.anydb.service.ConnectionManager;
import com.anydb.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private DatabaseService databaseService;
    
    @Autowired
    private ConnectionManager connectionManager;
    
    /**
     * 应用健康检查
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            health.put("version", "1.0.0");
            
            // 应用状态
            Map<String, Object> application = new HashMap<>();
            application.put("name", "AnyDB Web Backend");
            application.put("description", "开源数据库管理软件");
            application.put("connectionPools", databaseService.getConnectionPoolCount());
            health.put("application", application);
            
            // 系统信息
            Map<String, Object> system = new HashMap<>();
            system.put("java.version", System.getProperty("java.version"));
            system.put("os.name", System.getProperty("os.name"));
            system.put("os.version", System.getProperty("os.version"));
            system.put("os.arch", System.getProperty("os.arch"));
            health.put("system", system);
            
            log.debug("健康检查完成，连接池数量: {}", databaseService.getConnectionPoolCount());
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("健康检查异常", e);
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("timestamp", System.currentTimeMillis());
            health.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
    
    /**
     * 应用基本信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "AnyDB Web Backend");
        info.put("description", "开源数据库管理软件");
        info.put("version", "1.0.0");
        info.put("author", "AnyDB Team");
        info.put("features", new String[]{
            "支持多种数据库连接",
            "按需连接管理",
            "SQL编辑器",
            "性能监控",
            "数据库管理"
        });
        
        return ResponseEntity.ok(info);
    }
}
