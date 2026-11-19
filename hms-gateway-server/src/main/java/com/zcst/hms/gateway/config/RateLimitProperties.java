package com.zcst.hms.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关限流配置（支持配置中心热更新）
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "hms.rate-limit")
public class RateLimitProperties {

    /** 限流总开关 */
    private boolean enabled = true;

    /** 默认令牌桶容量 */
    private int capacity = 200;

    /** 默认每秒生成令牌数 */
    private int refreshRate = 100;

    /** 重点接口限流规则：路径 -> 每秒令牌数 */
    private Map<String, Integer> special = new HashMap<>();
}
