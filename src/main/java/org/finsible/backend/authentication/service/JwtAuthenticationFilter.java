package org.finsible.backend.authentication.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.finsible.backend.authentication.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private UserRepository userRepository;


    public JwtAuthenticationFilter() {
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String jwtFromCookie = extractJwtFromCookie(request);

        // Check for JWT in cookie if not in header
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else if (jwtFromCookie != null) {
            token = jwtFromCookie;
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = JwtService.validateToken(token);
            logger.info("Jwt token validation successful: User {} authenticated", claims.getSubject());
            String userId = claims.getSubject();
            // check in db if user exists
            userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            request.setAttribute("userId", userId);
            // Store user authentication in SecurityContextHolder
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            //ensures Spring Security recognizes the user and grants access.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException e) {
            logger.error("{} {}", e.getMessage(), getClass());
            clearAuthenticationCookies(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void clearAuthenticationCookies(HttpServletResponse response) {
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwt_token", "");
        jwtCookie.setHttpOnly(true);
        //jwtCookie.setSecure(false); // Set to true in production with HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // This expires the cookie immediately
        response.addCookie(jwtCookie);

        Cookie authStatusCookie = new Cookie("is_authenticated", "false");
        authStatusCookie.setHttpOnly(false);
        //authStatusCookie.setSecure(false); // Set to true in production with HTTPS
        authStatusCookie.setPath("/");
        authStatusCookie.setMaxAge(15552000);
        response.addCookie(authStatusCookie);

        logger.info("Authentication cookies updated due to invalid JWT");
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
