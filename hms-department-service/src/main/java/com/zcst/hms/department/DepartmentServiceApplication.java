package com.zcst.hms.department;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 科室服务启动类
 */
@EnableEurekaClient
@EnableFeignClients
@MapperScan("com.zcst.hms.department.mapper")
@SpringBootApplication(scanBasePackages = "com.zcst.hms")
public class DepartmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DepartmentServiceApplication.class, args);
    }
}
