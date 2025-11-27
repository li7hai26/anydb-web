package com.anydb.connector;

/**
 * 列信息类
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
public class ColumnInfo {
    
    /**
     * 列名
     */
    private String name;
    
    /**
     * 数据类型
     */
    private String type;
    
    /**
     * 是否可为空
     */
    private boolean nullable;
    
    /**
     * 是否为主键
     */
    private boolean primaryKey;
    
    /**
     * 是否为外键
     */
    private boolean foreignKey;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 最大长度
     */
    private int maxLength;
    
    /**
     * 精度（用于DECIMAL、NUMERIC等）
     */
    private int precision;
    
    /**
     * 小数位数（用于DECIMAL、NUMERIC等）
     */
    private int scale;
    
    /**
     * 列注释
     */
    private String comment;
    
    /**
     * 字符集
     */
    private String charset;
    
    /**
     * 排序规则
     */
    private String collation;
    
    public ColumnInfo() {
        this.nullable = true;
        this.primaryKey = false;
        this.foreignKey = false;
        this.maxLength = 0;
        this.precision = 0;
        this.scale = 0;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
    
    public boolean isForeignKey() {
        return foreignKey;
    }
    
    public void setForeignKey(boolean foreignKey) {
        this.foreignKey = foreignKey;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public int getMaxLength() {
        return maxLength;
    }
    
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    
    public int getPrecision() {
        return precision;
    }
    
    public void setPrecision(int precision) {
        this.precision = precision;
    }
    
    public int getScale() {
        return scale;
    }
    
    public void setScale(int scale) {
        this.scale = scale;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getCharset() {
        return charset;
    }
    
    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    public String getCollation() {
        return collation;
    }
    
    public void setCollation(String collation) {
        this.collation = collation;
    }
}