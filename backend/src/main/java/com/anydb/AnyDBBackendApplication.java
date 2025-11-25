package com.anydb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * AnyDB Web Backend Application
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
@EnableTransactionManagement
@MapperScan("com.anydb.mapper")
@EnableAsync
@EnableScheduling
public class AnyDBBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnyDBBackendApplication.class, args);
        System.out.println("""
                
                ╔═══════════════════════════════════════════════════════════════╗
                ║                        AnyDB Web Backend                      ║
                ║                    启动成功！数据库管理平台                     ║
                ║                                                               ║
                ║  🌐 Backend URL: http://localhost:8080/api                    ║
                ║  🔍 Druid监控: http://localhost:8080/api/druid               ║
                ║  💚 健康检查: http://localhost:8080/api/actuator/health       ║
                ║                                                               ║
                ╚═══════════════════════════════════════════════════════════════╝
                """);
    }
}