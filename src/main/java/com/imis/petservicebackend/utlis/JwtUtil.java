package com.imis.petservicebackend.utlis;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
public class JwtUtil {

    // 密钥（必须 >= 256 bits = 32字节）
    private static final String SECRET = "12345678901234567890123456789012"; // 32位
    private static final long EXPIRATION = 1000 * 60 * 60 * 24 * 3; // 3天

    private static final SecretKeySpec KEY = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");

    public static String generateToken(String userAccount) {
        return Jwts.builder()
                .setSubject(userAccount)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, KEY)
                .compact();

    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(KEY)
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public static String getUserAccount(String token) {
        return Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public static void main(String[] args) {
        String token = generateToken("admin@qq.com");
        System.out.println("Token: " + token);
        System.out.println("Valid: " + validateToken(token));
        System.out.println("UserAccount: " + getUserAccount(token));
    }
}