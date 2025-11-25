package com.anydb.connector.impl;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseConnector;
import com.anydb.connector.DatabaseType;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

/**
 * Redis数据库连接器实现
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
public class RedisConnector implements DatabaseConnector {
    
    private JedisPool jedisPool;
    
    @Override
    public DatabaseType getSupportedType() {
        return DatabaseType.REDIS;
    }
    
    @Override
    public boolean testConnection(DatabaseConfig config) {
        try (Jedis jedis = getJedis(config)) {
            return jedis != null && jedis.ping().equals("PONG");
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public QueryResult executeQuery(DatabaseConfig config, String sql) {
        long startTime = System.currentTimeMillis();
        
        try (Jedis jedis = getJedis(config)) {
            // 解析简单的Redis命令
            String[] parts = sql.trim().split("\\s+");
            String command = parts[0].toUpperCase();
            
            switch (command) {
                case "KEYS":
                    return executeKeys(jedis, parts);
                case "GET":
                    return executeGet(jedis, parts);
                case "HGETALL":
                    return executeHGetAll(jedis, parts);
                case "LRANGE":
                    return executeLRange(jedis, parts);
                case "SMEMBERS":
                    return executeSMembers(jedis, parts);
                case "INFO":
                    return executeInfo(jedis);
                default:
                    throw new IllegalArgumentException("不支持的Redis命令: " + command);
            }
        } catch (Exception e) {
            throw new RuntimeException("Redis命令执行失败: " + e.getMessage(), e);
        } finally {
            closePool();
        }
    }
    
    @Override
    public UpdateResult executeUpdate(DatabaseConfig config, String sql) {
        long startTime = System.currentTimeMillis();
        
        try (Jedis jedis = getJedis(config)) {
            String[] parts = sql.trim().split("\\s+");
            String command = parts[0].toUpperCase();
            
            long affectedRows = 0;
            
            switch (command) {
                case "SET":
                    String key = parts[1];
                    String value = parts[2];
                    if (parts.length > 4 && "EX".equals(parts[3].toUpperCase())) {
                        long seconds = Long.parseLong(parts[4]);
                        jedis.setex(key, seconds, value);
                    } else {
                        jedis.set(key, value);
                    }
                    affectedRows = 1;
                    break;
                    
                case "DEL":
                    affectedRows = jedis.del(parts[1]);
                    break;
                    
                case "HSET":
                    if (parts.length >= 4) {
                        affectedRows = jedis.hset(parts[1], parts[2], parts[3]);
                    }
                    break;
                    
                case "LPUSH":
                    String listKey = parts[1];
                    for (int i = 2; i < parts.length; i++) {
                        jedis.lpush(listKey, parts[i]);
                    }
                    affectedRows = parts.length - 2;
                    break;
                    
                case "SADD":
                    String setKey = parts[1];
                    for (int i = 2; i < parts.length; i++) {
                        jedis.sadd(setKey, parts[i]);
                    }
                    affectedRows = parts.length - 2;
                    break;
                    
                default:
                    throw new IllegalArgumentException("不支持的Redis命令: " + command);
            }
            
            UpdateResult result = new UpdateResult();
            result.setAffectedRows((int) affectedRows);
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            result.setMessage("操作成功");
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Redis命令执行失败: " + e.getMessage(), e);
        } finally {
            closePool();
        }
    }
    
    @Override
    public List<String> getDatabases(DatabaseConfig config) {
        try (Jedis jedis = getJedis(config)) {
            List<String> databases = new ArrayList<>();
            
            // Redis默认有16个数据库（0-15）
            for (int i = 0; i < 16; i++) {
                databases.add(String.valueOf(i));
            }
            
            return databases;
            
        } catch (Exception e) {
            throw new RuntimeException("获取数据库列表失败: " + e.getMessage(), e);
        } finally {
            closePool();
        }
    }
    
    @Override
    public List<TableInfo> getTables(DatabaseConfig config, String database) {
        try (Jedis jedis = getJedis(config)) {
            List<TableInfo> tables = new ArrayList<>();
            
            // 在Redis中，"表"实际上是key
            ScanParams scanParams = new ScanParams().match("*").count(100);
            ScanResult<String> scanResult = jedis.scan("0", scanParams);
            
            for (String key : scanResult.getResult()) {
                TableInfo table = new TableInfo();
                table.setName(key);
                table.setComment("Redis Key");
                
                // 获取key的详细信息
                String type = jedis.type(key);
                long ttl = jedis.ttl(key);
                
                table.setRowCount(1); // Redis每个key只有一条记录
                table.setCreateTime(0); // Redis不直接提供key创建时间
                
                tables.add(table);
            }
            
            return tables;
            
        } catch (Exception e) {
            throw new RuntimeException("获取key列表失败: " + e.getMessage(), e);
        } finally {
            closePool();
        }
    }
    
    @Override
    public TableInfo getTableInfo(DatabaseConfig config, String database, String tableName) {
        try (Jedis jedis = getJedis(config)) {
            TableInfo table = new TableInfo();
            table.setName(tableName);
            
            String type = jedis.type(tableName);
            table.setComment("Redis Key Type: " + type);
            
            // 根据类型获取详细信息
            List<ColumnInfo> columns = new ArrayList<>();
            
            switch (type) {
                case "string":
                    String value = jedis.get(tableName);
                    columns.add(new ColumnInfo("value", "string", "String value", true, false, null, null, null, value));
                    break;
                    
                case "hash":
                    Map<String, String> hashData = jedis.hgetAll(tableName);
                    for (Map.Entry<String, String> entry : hashData.entrySet()) {
                        columns.add(new ColumnInfo(entry.getKey(), "string", "Hash field", true, false, null, null, null, entry.getValue()));
                    }
                    break;
                    
                case "list":
                    long listSize = jedis.llen(tableName);
                    columns.add(new ColumnInfo("size", "integer", "List size", false, false, null, null, null, String.valueOf(listSize)));
                    
                    List<String> listValues = jedis.lrange(tableName, 0, 9);
                    for (int i = 0; i < listValues.size(); i++) {
                        columns.add(new ColumnInfo("element_" + i, "string", "List element", true, false, null, null, null, listValues.get(i)));
                    }
                    break;
                    
                case "set":
                    Set<String> setMembers = jedis.smembers(tableName);
                    for (String member : setMembers) {
                        columns.add(new ColumnInfo("member", "string", "Set member", true, false, null, null, null, member));
                    }
                    break;
                    
                case "zset":
                    Map<String, Double> zsetData = jedis.zrangeWithScores(tableName, 0, -1);
                    for (Map.Entry<String, Double> entry : zsetData.entrySet()) {
                        columns.add(new ColumnInfo(entry.getKey(), "double", "Zset score", true, false, null, null, null, entry.getValue().toString()));
                    }
                    break;
            }
            
            table.setColumns(columns);
            table.setRowCount(1);
            
            return table;
            
        } catch (Exception e) {
            throw new RuntimeException("获取key信息失败: " + e.getMessage(), e);
        } finally {
            closePool();
        }
    }
    
    @Override
    public QueryResult getTableData(DatabaseConfig config, String database, String tableName, 
                                  int page, int size, String orderBy, String orderDirection) {
        long startTime = System.currentTimeMillis();
        
        try (Jedis jedis = getJedis(config)) {
            List<String> columns = Arrays.asList("key", "type", "value", "ttl");
            List<List<Object>> rows = new ArrayList<>();
            
            String type = jedis.type(tableName);
            String value = "";
            long ttl = jedis.ttl(tableName);
            
            switch (type) {
                case "string":
                    value = jedis.get(tableName);
                    break;
                case "hash":
                    value = jedis.hgetAll(tableName).toString();
                    break;
                case "list":
                    value = jedis.lrange(tableName, 0, 10).toString();
                    break;
                case "set":
                    value = jedis.smembers(tableName).toString();
                    break;
                case "zset":
                    value = jedis.zrangeWithScores(tableName, 0, 10).toString();
                    break;
            }
            
            List<Object> row = Arrays.asList(tableName, type, value, ttl);
            rows.add(row);
            
            QueryResult result = new QueryResult();
            result.setColumns(columns);
            result.setRows(rows);
            result.setTotal(1);
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("获取key数据失败: " + e.getMessage(), e);
        } finally {
            closePool();
        }
    }
    
    @Override
    public void close() {
        closePool();
    }
    
    /**
     * 获取Redis连接
     */
    private Jedis getJedis(DatabaseConfig config) {
        if (jedisPool == null) {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(20);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(2);
            
            String host = config.getHost();
            int port = config.getPort() > 0 ? config.getPort() : 6379;
            String password = config.getPassword();
            int database = 0;
            
            if (password != null && !password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, 2000, password, database);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port);
            }
        }
        
        return jedisPool.getResource();
    }
    
    /**
     * 关闭连接池
     */
    private void closePool() {
        // 不在这里关闭pool，由try-with-resources自动管理
    }
    
    /**
     * 执行KEYS命令
     */
    private QueryResult executeKeys(Jedis jedis, String[] parts) {
        String pattern = parts.length > 1 ? parts[1] : "*";
        Set<String> keys = jedis.keys(pattern);
        
        List<String> columns = Arrays.asList("key", "type", "ttl");
        List<List<Object>> rows = new ArrayList<>();
        
        for (String key : keys) {
            String type = jedis.type(key);
            long ttl = jedis.ttl(key);
            rows.add(Arrays.asList(key, type, ttl));
        }
        
        QueryResult result = new QueryResult();
        result.setColumns(columns);
        result.setRows(rows);
        result.setTotal(keys.size());
        
        return result;
    }
    
    /**
     * 执行GET命令
     */
    private QueryResult executeGet(Jedis jedis, String[] parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException("GET命令需要key参数");
        }
        
        String key = parts[1];
        String value = jedis.get(key);
        
        List<String> columns = Arrays.asList("key", "value", "type", "ttl");
        List<List<Object>> rows = Arrays.asList(
            Arrays.asList(key, value, jedis.type(key), jedis.ttl(key))
        );
        
        QueryResult result = new QueryResult();
        result.setColumns(columns);
        result.setRows(rows);
        result.setTotal(1);
        
        return result;
    }
    
    /**
     * 执行HGETALL命令
     */
    private QueryResult executeHGetAll(Jedis jedis, String[] parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException("HGETALL命令需要key参数");
        }
        
        String key = parts[1];
        Map<String, String> hashData = jedis.hgetAll(key);
        
        List<String> columns = Arrays.asList("field", "value");
        List<List<Object>> rows = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : hashData.entrySet()) {
            rows.add(Arrays.asList(entry.getKey(), entry.getValue()));
        }
        
        QueryResult result = new QueryResult();
        result.setColumns(columns);
        result.setRows(rows);
        result.setTotal(hashData.size());
        
        return result;
    }
    
    /**
     * 执行LRANGE命令
     */
    private QueryResult executeLRange(Jedis jedis, String[] parts) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("LRANGE命令需要key, start, end参数");
        }
        
        String key = parts[1];
        long start = Long.parseLong(parts[2]);
        long end = Long.parseLong(parts[3]);
        
        List<String> listValues = jedis.lrange(key, start, end);
        
        List<String> columns = Arrays.asList("index", "value");
        List<List<Object>> rows = new ArrayList<>();
        
        for (int i = 0; i < listValues.size(); i++) {
            rows.add(Arrays.asList(start + i, listValues.get(i)));
        }
        
        QueryResult result = new QueryResult();
        result.setColumns(columns);
        result.setRows(rows);
        result.setTotal(listValues.size());
        
        return result;
    }
    
    /**
     * 执行SMEMBERS命令
     */
    private QueryResult executeSMembers(Jedis jedis, String[] parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException("SMEMBERS命令需要key参数");
        }
        
        String key = parts[1];
        Set<String> members = jedis.smembers(key);
        
        List<String> columns = Arrays.asList("member");
        List<List<Object>> rows = new ArrayList<>();
        
        for (String member : members) {
            rows.add(Arrays.asList(member));
        }
        
        QueryResult result = new QueryResult();
        result.setColumns(columns);
        result.setRows(rows);
        result.setTotal(members.size());
        
        return result;
    }
    
    /**
     * 执行INFO命令
     */
    private QueryResult executeInfo(Jedis jedis) {
        Properties info = jedis.info();
        
        List<String> columns = Arrays.asList("key", "value");
        List<List<Object>> rows = new ArrayList<>();
        
        for (Map.Entry<Object, Object> entry : info.entrySet()) {
            rows.add(Arrays.asList(entry.getKey().toString(), entry.getValue().toString()));
        }
        
        QueryResult result = new QueryResult();
        result.setColumns(columns);
        result.setRows(rows);
        result.setTotal(info.size());
        
        return result;
    }
}