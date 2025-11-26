package com.anydb.controller;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseType;
import com.anydb.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理控制器
 * 
 * 功能：
 * - 数据库配置管理（增删改查）
 * - 连接测试
 * - SQL执行
 * - 库表信息查询
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/databases")
@CrossOrigin(origins = "*")
public class DatabaseController {
    
    @Autowired
    private DatabaseService databaseService;
    
    /**
     * 获取支持的数据库类型列表
     */
    @GetMapping("/types")
    public ResponseEntity<List<DatabaseType>> getSupportedTypes() {
        List<DatabaseType> types = databaseService.getSupportedTypes();
        log.info("获取支持的数据库类型，数量: {}", types.size());
        return ResponseEntity.ok(types);
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API is working!");
    }
    
    /**
     * 测试数据库连接
     */
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody DatabaseConfig config) {
        try {
            log.info("测试数据库连接: {} - {}", config.getType(), config.getHost());
            
            boolean success = databaseService.testConnection(config);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "连接成功" : "连接失败");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试数据库连接异常", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "连接测试异常: " + e.getMessage());
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 创建数据库连接池（按需）
     */
    @PostMapping("/connection-pools")
    public ResponseEntity<Map<String, Object>> createConnectionPool(
            @RequestParam Long configId, 
            @RequestBody DatabaseConfig config) {
        try {
            log.info("创建数据库连接池，配置ID: {}, 类型: {}", configId, config.getType());
            
            databaseService.createConnectionPool(configId, config);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "连接池创建成功");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("创建数据库连接池失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "连接池创建失败: " + e.getMessage());
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 关闭数据库连接池
     */
    @DeleteMapping("/connection-pools/{configId}")
    public ResponseEntity<Map<String, Object>> closeConnectionPool(@PathVariable Long configId) {
        try {
            log.info("关闭数据库连接池，配置ID: {}", configId);
            
            databaseService.closeConnectionPool(configId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "连接池关闭成功");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("关闭数据库连接池失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "连接池关闭失败: " + e.getMessage());
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 获取连接池状态
     */
    @GetMapping("/connection-pools/status")
    public ResponseEntity<Map<String, Object>> getConnectionPoolStatus() {
        try {
            int poolCount = databaseService.getConnectionPoolCount();
            
            Map<String, Object> result = new HashMap<>();
            result.put("activePools", poolCount);
            result.put("status", "健康");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取连接池状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 执行SQL查询
     */
    @PostMapping("/{configId}/execute-query")
    public ResponseEntity<Object> executeQuery(
            @PathVariable Long configId, 
            @RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "SQL语句不能为空"));
            }
            
            log.info("执行SQL查询，配置ID: {}, SQL: {}", configId, sql);
            
            var result = databaseService.executeQuery(configId, sql);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行SQL查询失败", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "SQL执行失败: " + e.getMessage());
            
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * 执行SQL更新
     */
    @PostMapping("/{configId}/execute-update")
    public ResponseEntity<Object> executeUpdate(
            @PathVariable Long configId, 
            @RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "SQL语句不能为空"));
            }
            
            log.info("执行SQL更新，配置ID: {}, SQL: {}", configId, sql);
            
            var result = databaseService.executeUpdate(configId, sql);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行SQL更新失败", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "SQL执行失败: " + e.getMessage());
            
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * 获取数据库列表
     */
    @GetMapping("/{configId}/databases")
    public ResponseEntity<Object> getDatabases(@PathVariable Long configId) {
        try {
            log.info("获取数据库列表，配置ID: {}", configId);
            
            List<String> databases = databaseService.getDatabases(configId);
            
            return ResponseEntity.ok(databases);
        } catch (Exception e) {
            log.error("获取数据库列表失败", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取数据库列表失败: " + e.getMessage());
            
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * 获取表列表
     */
    @GetMapping("/{configId}/tables")
    public ResponseEntity<Object> getTables(
            @PathVariable Long configId, 
            @RequestParam(required = false) String database) {
        try {
            log.info("获取表列表，配置ID: {}, 数据库: {}", configId, database);
            
            var tables = databaseService.getTables(configId, database);
            
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("获取表列表失败", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取表列表失败: " + e.getMessage());
            
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * 获取表结构信息
     */
    @GetMapping("/{configId}/table-info")
    public ResponseEntity<Object> getTableInfo(
            @PathVariable Long configId, 
            @RequestParam String database,
            @RequestParam String tableName) {
        try {
            log.info("获取表结构信息，配置ID: {}, 数据库: {}, 表: {}", configId, database, tableName);
            
            var tableInfo = databaseService.getTableInfo(configId, database, tableName);
            
            return ResponseEntity.ok(tableInfo);
        } catch (Exception e) {
            log.error("获取表结构信息失败", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取表结构信息失败: " + e.getMessage());
            
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * 获取表数据（分页）
     */
    @GetMapping("/{configId}/table-data")
    public ResponseEntity<Object> getTableData(
            @PathVariable Long configId, 
            @RequestParam String database,
            @RequestParam String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false, defaultValue = "ASC") String orderDirection) {
        try {
            log.info("获取表数据，配置ID: {}, 数据库: {}, 表: {}, 页码: {}, 每页: {}", 
                    configId, database, tableName, page, size);
            
            var result = databaseService.getTableData(configId, database, tableName, 
                                                   page, size, orderBy, orderDirection);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取表数据失败", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取表数据失败: " + e.getMessage());
            
            return ResponseEntity.ok(error);
        }
    }
}
