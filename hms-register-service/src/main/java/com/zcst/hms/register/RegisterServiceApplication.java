package com.zcst.hms.register;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 挂号服务启动类（服务编排核心）
 */
@EnableEurekaClient
@EnableFeignClients
@EnableHystrix
@MapperScan("com.zcst.hms.register.mapper")
@SpringBootApplication(scanBasePackages = "com.zcst.hms")
public class RegisterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegisterServiceApplication.class, args);
    }
}
