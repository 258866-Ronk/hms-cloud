package com.zcst.hms.register;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局 CORS 配置（Filter 级别，覆盖所有端点）。
 * 主要用途：允许 Hystrix Dashboard（默认 8070 端口）跨域读取
 * /actuator/hystrix.stream 监控流。普通的 management.endpoints.web.cors
 * 配置对流式 Servlet 端点不生效，因此这里使用 CorsFilter 放行所有来源。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
