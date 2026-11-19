package com.zcst.hms.schedule;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 排班服务启动类
 */
@EnableEurekaClient
@EnableFeignClients
@MapperScan("com.zcst.hms.schedule.mapper")
@SpringBootApplication(scanBasePackages = "com.zcst.hms")
public class ScheduleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleServiceApplication.class, args);
    }
}
