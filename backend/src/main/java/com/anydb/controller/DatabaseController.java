package com.anydb.controller;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseConnector;
import com.anydb.connector.DatabaseType;
import com.anydb.service.DatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据库管理控制器
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/databases")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DatabaseController {
    
    private final DatabaseService databaseService;
    
    /**
     * 获取支持的数据库类型
     */
    @GetMapping("/types")
    public ResponseEntity<List<DatabaseType>> getSupportedTypes() {
        return ResponseEntity.ok(databaseService.getSupportedTypes());
    }
    
    /**
     * 测试数据库连接
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody DatabaseConfig config) {
        boolean success = databaseService.testConnection(config);
        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "连接成功" : "连接失败"
        ));
    }
    
    /**
     * 获取数据库列表
     */
    @PostMapping("/{configId}/databases")
    public ResponseEntity<List<String>> getDatabases(@PathVariable Long configId) {
        return ResponseEntity.ok(databaseService.getDatabases(configId));
    }
    
    /**
     * 获取表列表
     */
    @PostMapping("/{configId}/tables")
    public ResponseEntity<List<DatabaseConnector.TableInfo>> getTables(
            @PathVariable Long configId,
            @RequestParam(required = false) String database) {
        return ResponseEntity.ok(databaseService.getTables(configId, database));
    }
    
    /**
     * 获取表结构信息
     */
    @PostMapping("/{configId}/table-info")
    public ResponseEntity<DatabaseConnector.TableInfo> getTableInfo(
            @PathVariable Long configId,
            @RequestParam String database,
            @RequestParam String tableName) {
        return ResponseEntity.ok(databaseService.getTableInfo(configId, database, tableName));
    }
    
    /**
     * 获取表数据
     */
    @PostMapping("/{configId}/table-data")
    public ResponseEntity<DatabaseConnector.QueryResult> getTableData(
            @PathVariable Long configId,
            @RequestParam String database,
            @RequestParam String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {
        return ResponseEntity.ok(databaseService.getTableData(configId, database, tableName, page, size, orderBy, orderDirection));
    }
    
    /**
     * 执行SQL查询
     */
    @PostMapping("/{configId}/query")
    public ResponseEntity<DatabaseConnector.QueryResult> executeQuery(
            @PathVariable Long configId,
            @RequestParam String sql) {
        return ResponseEntity.ok(databaseService.executeQuery(configId, sql));
    }
    
    /**
     * 执行SQL更新
     */
    @PostMapping("/{configId}/update")
    public ResponseEntity<DatabaseConnector.UpdateResult> executeUpdate(
            @PathVariable Long configId,
            @RequestParam String sql) {
        return ResponseEntity.ok(databaseService.executeUpdate(configId, sql));
    }
}