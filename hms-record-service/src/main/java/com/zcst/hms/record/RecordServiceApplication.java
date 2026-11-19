package com.zcst.hms.record;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 电子病历服务启动类
 */
@EnableEurekaClient
@EnableFeignClients
@MapperScan("com.zcst.hms.record.mapper")
@SpringBootApplication(scanBasePackages = "com.zcst.hms")
public class RecordServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecordServiceApplication.class, args);
    }
}
