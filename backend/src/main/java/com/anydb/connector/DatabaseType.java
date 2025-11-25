package com.anydb.connector;

/**
 * 数据库类型枚举
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
public enum DatabaseType {
    
    MYSQL("mysql", "MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://"),
    POSTGRESQL("postgresql", "PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://"),
    ORACLE("oracle", "Oracle", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@"),
    MARIADB("mariadb", "MariaDB", "org.mariadb.jdbc.Driver", "jdbc:mariadb://"),
    SQLSERVER("sqlserver", "SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://"),
    REDIS("redis", "Redis", "redis.clients.jedis.Jedis", "redis://"),
    ELASTICSEARCH("elasticsearch", "Elasticsearch", "org.elasticsearch.client.RestHighLevelClient", "http://"),
    MONGODB("mongodb", "MongoDB", "com.mongodb.MongoClient", "mongodb://"),
    ETCD("etcd", "Etcd", "io.etcd.jetcd.Client", "etcd://"),
    TDENGINE("tdengine", "TDEngine", "com.taosdata.jdbc.TaosDriver", "jdbc:TAOS://"),
    KAFKA("kafka", "Apache Kafka", "org.apache.kafka.clients.consumer.KafkaConsumer", ""),
    ZOOKEEPER("zookeeper", "Zookeeper", "org.apache.zookeeper.ZooKeeper", ""),
    TIDB("tidb", "TiDB", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://"),
    OCEANBASE("oceanbase", "OceanBase", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://"),
    DB2("db2", "DB2", "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://"),
    CLICKHOUSE("clickhouse", "ClickHouse", "com.clickhouse.jdbc.ClickHouseDriver", "jdbc:clickhouse://"),
    PRESTO("presto", "Presto", "com.facebook.presto.jdbc.PrestoDriver", "jdbc:presto://"),
    TRINO("trino", "Trino", "io.trino.jdbc.TrinoDriver", "jdbc:trino://");
    
    private final String code;
    private final String displayName;
    private final String driverClass;
    private final String urlPrefix;
    
    DatabaseType(String code, String displayName, String driverClass, String urlPrefix) {
        this.code = code;
        this.displayName = displayName;
        this.driverClass = driverClass;
        this.urlPrefix = urlPrefix;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDriverClass() {
        return driverClass;
    }
    
    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    public static DatabaseType fromCode(String code) {
        for (DatabaseType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown database type: " + code);
    }
}