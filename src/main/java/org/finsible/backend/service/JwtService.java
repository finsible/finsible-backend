package org.finsible.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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

    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public static String generateToken(String userId, List<String> roles) {
        logger.info("Generating token for user {}", userId);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public static Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature");
            throw new RuntimeException("Invalid JWT signature");
        }
    }
}
