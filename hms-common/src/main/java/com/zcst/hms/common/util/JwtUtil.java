package com.zcst.hms.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具类（网关与用户服务共用）
 */
public class JwtUtil {

    public static final String SECRET = "hms-cloud-secret-2026";
    public static final long EXPIRE = 1000L * 60 * 60 * 8;

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_REAL_NAME = "realName";

    public static String createToken(Long userId, String username, String role, String realName) {
        Map<String, Object> claims = new HashMap<>(8);
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_REAL_NAME, realName);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
    }

    public static Long getUserId(String token) {
        Object v = parseToken(token).get(CLAIM_USER_ID);
        return v == null ? null : Long.valueOf(String.valueOf(v));
    }

    public static String getRole(String token) {
        Object v = parseToken(token).get(CLAIM_ROLE);
        return v == null ? null : String.valueOf(v);
    }

    public static String getUsername(String token) {
        Object v = parseToken(token).get(CLAIM_USERNAME);
        return v == null ? null : String.valueOf(v);
    }

    public static String getRealName(String token) {
        Object v = parseToken(token).get(CLAIM_REAL_NAME);
        return v == null ? null : String.valueOf(v);
    }
}
