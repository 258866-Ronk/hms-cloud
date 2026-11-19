package com.zcst.hms.gateway.filter;

import com.zcst.hms.gateway.config.RateLimitProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网关限流过滤器：基于令牌桶算法，对重点接口（如挂号）按 IP 限流，防止号源被刷。
 */
@Component
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private final RateLimitProperties properties;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitGlobalFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        double baseRate = properties.getRefreshRate();
        double baseCapacity = properties.getCapacity();
        Map<String, Integer> special = properties.getSpecial();
        if (special != null && special.containsKey(path)) {
            baseRate = special.get(path);
            baseCapacity = Math.max(baseRate, 10);
        }
        final double rate = baseRate;
        final double capacity = baseCapacity;

        String ip = resolveIp(request);
        String key = path + ":" + ip;
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, rate));

        if (!bucket.tryAcquire()) {
            return tooManyRequests(exchange, path, rate);
        }
        return chain.filter(exchange);
    }

    private String resolveIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            java.net.InetSocketAddress addr = request.getRemoteAddress();
            ip = addr == null ? "unknown" : addr.getAddress().getHostAddress();
        }
        return ip;
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange, String path, double rate) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        String body = "{\"code\":429,\"message\":\"请求过于频繁，" + path + " 限流 " + (long) rate
                + " 次/秒，请稍后再试\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 简易令牌桶
     */
    static class TokenBucket {
        private final double capacity;
        private final double rate;
        private double tokens;
        private long lastRefillNano;

        TokenBucket(double capacity, double rate) {
            this.capacity = capacity;
            this.rate = rate;
            this.tokens = capacity;
            this.lastRefillNano = System.nanoTime();
        }

        synchronized boolean tryAcquire() {
            long now = System.nanoTime();
            double elapsed = (now - lastRefillNano) / 1_000_000_000.0;
            tokens = Math.min(capacity, tokens + elapsed * rate);
            lastRefillNano = now;
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
