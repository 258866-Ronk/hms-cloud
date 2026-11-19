package com.zcst.hms.gateway.filter;

import com.zcst.hms.common.constants.HmsConstants;
import com.zcst.hms.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 全局鉴权过滤器：校验 JWT 令牌，并将用户信息透传给下游微服务
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /** 无需登录即可访问的路径 */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/dept/list",
            "/api/dept/tree",
            "/api/doctor/list",
            "/api/schedule/list",
            "/api/schedule/available",
            "/fallback/**",
            "/",
            "/index.html",
            "/favicon.ico",
            "/css/**",
            "/js/**",
            "/img/**",
            "/actuator/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhite(path) || org.springframework.http.HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "请先登录后再访问该接口");
        }
        try {
            Claims claims = JwtUtil.parseToken(token);
            ServerHttpRequest mutated = request.mutate()
                    .header(HmsConstants.HEADER_USER_ID, String.valueOf(claims.get(JwtUtil.CLAIM_USER_ID)))
                    .header(HmsConstants.HEADER_USERNAME, String.valueOf(claims.get(JwtUtil.CLAIM_USERNAME)))
                    .header(HmsConstants.HEADER_ROLE, String.valueOf(claims.get(JwtUtil.CLAIM_ROLE)))
                    .header(HmsConstants.HEADER_REAL_NAME, String.valueOf(claims.get(JwtUtil.CLAIM_REAL_NAME)))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            return unauthorized(exchange, "登录已失效或令牌非法");
        }
    }

    private String resolveToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(HmsConstants.HEADER_TOKEN);
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }

    private boolean isWhite(String path) {
        for (String pattern : WHITE_LIST) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
