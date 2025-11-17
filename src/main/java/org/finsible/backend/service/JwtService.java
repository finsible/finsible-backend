package org.finsible.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
    private static final String secretKey= System.getenv("SECRET_KEY");
    private static final long jwtExpiration= Long.parseLong(System.getenv("JWT_EXPIRATION"));
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public static String generateToken(String userId, List<String> roles) {
        Key key = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName()); //return "HmacSHA256
        logger.info("Generating token for user {}", userId);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return Jwts.builder()
                .setClaims(claims)
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
            logger.error("Invalid JWT signature");
            throw new RuntimeException("Invalid JWT signature");
        }
    }
}
