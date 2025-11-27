package com.anydb.connector;

import java.util.List;

/**
 * 表信息类
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
public class TableInfo {
    
    /**
     * 表名
     */
    private String name;
    
    /**
     * 表注释
     */
    private String comment;
    
    /**
     * 表类型
     */
    private String type;
    
    /**
     * 行数
     */
    private int rowCount;
    
    /**
     * 表大小（字节）
     */
    private long size;
    
    /**
     * 最后更新时间
     */
    private long updateTime;
    
    /**
     * 列信息列表
     */
    private List<ColumnInfo> columns;
    
    /**
     * 是否是临时表
     */
    private boolean temporary;
    
    public TableInfo() {
        this.rowCount = 0;
        this.size = 0;
        this.updateTime = 0;
        this.temporary = false;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getRowCount() {
        return rowCount;
    }
    
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public long getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    
    public List<ColumnInfo> getColumns() {
        return columns;
    }
    
    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }
    
    public boolean isTemporary() {
        return temporary;
    }
    
    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }
}