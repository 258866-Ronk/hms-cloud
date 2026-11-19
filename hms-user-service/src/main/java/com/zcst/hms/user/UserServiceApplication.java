package com.zcst.hms.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户 / 认证服务启动类
 */
@EnableEurekaClient
@EnableFeignClients
@MapperScan("com.zcst.hms.user.mapper")
@SpringBootApplication(scanBasePackages = "com.zcst.hms")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
