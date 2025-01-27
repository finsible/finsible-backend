package org.finsible.backend.authentication.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private static final String secretKey= System.getenv("SECRET_KEY");
    private static final long jwtExpiration= Long.parseLong(System.getenv("JWT_EXPIRATION"));

    public static String generateToken(String userId) {
        Key key = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    public static Claims validateToken(String token) {
        try {
            Key key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature");
        }
    }
}
