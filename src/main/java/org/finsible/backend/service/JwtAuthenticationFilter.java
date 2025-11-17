package org.finsible.backend.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.finsible.backend.CustomExceptionHandler.UserNotFoundException;
import org.finsible.backend.repository.UserRepository;
import org.finsible.backend.utility.CookieHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final UserRepository userRepository;
    private final CookieHandler cookieHandler;


    public JwtAuthenticationFilter(UserRepository userRepository, CookieHandler cookieHandler) {
        this.userRepository = userRepository;
        this.cookieHandler = cookieHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String jwtFromCookie = cookieHandler.extractJwtToken(request);

        // Check for JWT in cookie if not in header
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        if (jwtFromCookie != null) {
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
            userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
            request.setAttribute("userId", userId);

            // Store user authentication in SecurityContextHolder
            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles != null
                ? roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList()
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

            //ensures Spring Security recognizes the user and grants access.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException e) {
            logger.error("{} {}", e.getMessage(), getClass());
            cookieHandler.clearAuthenticationCookies(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid request");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
