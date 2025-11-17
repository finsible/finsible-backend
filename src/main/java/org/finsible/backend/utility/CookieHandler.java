package org.finsible.backend.utility;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieHandler {

    // Cookie names as constants
    public static final String JWT_COOKIE_NAME = "jwt_token";
    public static final String AUTH_STATUS_COOKIE_NAME = "is_authenticated";

    // Default expiry times
    public static final int JWT_EXPIRY_SECONDS = 15552000; // 180 days
    public static final int AUTH_STATUS_EXPIRY_SECONDS = 15552000; // 180 days

    @Value("${app.cookie.secure:true}")
    private boolean secure;

    @Value("${app.cookie.same-site:Strict}")
    private String sameSite;

    /**
     * Creates a JWT cookie with the given token
     * @param jwtToken The JWT token to store
     * @param expiryInSeconds Cookie expiry time in seconds
     * @return Cookie configured for JWT storage
     */
    @NotNull
    public Cookie createJwtCookie(String jwtToken, int expiryInSeconds) {
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, jwtToken);

        // HTTP-only prevents JavaScript access - critical for XSS protection
        jwtCookie.setHttpOnly(true);

        // Secure flag ensures cookie only sent over HTTPS
        if(secure) {
            jwtCookie.setSecure(true);
        }

        // SameSite=Strict prevents CSRF attacks
        if (sameSite != null) {
            jwtCookie.setAttribute("SameSite", sameSite);
        }

        jwtCookie.setMaxAge(expiryInSeconds);
        jwtCookie.setPath("/");

        return jwtCookie;
    }

    /**
     * Creates an authentication status cookie that JavaScript can read
     * @param isAuthenticated Authentication status ("true" or "false")
     * @return Cookie for authentication status
     */
    @NotNull
    public Cookie createAuthStatusCookie(String isAuthenticated) {
        Cookie authStatusCookie = new Cookie(AUTH_STATUS_COOKIE_NAME, isAuthenticated);

        // JavaScript can read this one
        authStatusCookie.setHttpOnly(false);

        if(secure) {
            authStatusCookie.setSecure(true);
        }
        if(sameSite != null) {
            authStatusCookie.setAttribute("SameSite", sameSite);
        }

        authStatusCookie.setMaxAge(AUTH_STATUS_EXPIRY_SECONDS);
        authStatusCookie.setPath("/");
        return authStatusCookie;
    }

    /**
     * Creates a generic cookie with custom settings
     * @param name Cookie name
     * @param value Cookie value
     * @param maxAge Expiry time in seconds
     * @param httpOnly Whether cookie should be HTTP-only
     * @return Configured cookie
     */
    @NotNull
    public Cookie createCookie(String name, String value, int maxAge, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        // cookie.setSecure(true); // Uncomment for production

        return cookie;
    }

    /**
     * Extracts a cookie value by name from the request
     * @param request HTTP request
     * @param cookieName Name of the cookie to extract
     * @return Cookie value if found, null otherwise
     */
    @Nullable
    public String extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    /**
     * Extracts JWT token from cookies
     * @param request HTTP request
     * @return JWT token if found, null otherwise
     */
    @Nullable
    public String extractJwtToken(HttpServletRequest request) {
        return extractCookie(request, JWT_COOKIE_NAME);
    }

    /**
     * Extracts authentication status from cookies
     * @param request HTTP request
     * @return Authentication status if found, null otherwise
     */
    @Nullable
    public String extractAuthStatus(HttpServletRequest request) {
        return extractCookie(request, AUTH_STATUS_COOKIE_NAME);
    }

    /**
     * Gets a Cookie object by name from the request
     * @param request HTTP request
     * @param cookieName Name of the cookie
     * @return Optional containing the Cookie if found
     */
    public Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst();
    }

    /**
     * Removes/expires a cookie by setting its max age to 0
     * @param response HTTP response
     * @param cookieName Name of the cookie to remove
     */
    public void removeCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Removes/expires a cookie with specific settings
     * @param response HTTP response
     * @param cookieName Name of the cookie to remove
     * @param httpOnly Whether the cookie was HTTP-only
     */
    public void removeCookie(HttpServletResponse response, String cookieName, boolean httpOnly) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Clears all authentication cookies (JWT and auth status)
     * @param response HTTP response
     */
    public void clearAuthenticationCookies(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie jwtCookie = createJwtCookie("", 0);
        response.addCookie(jwtCookie);

        // Set auth status to false
        Cookie authStatusCookie = createAuthStatusCookie("false");
        response.addCookie(authStatusCookie);
    }

    /**
     * Sets authentication cookies (JWT and auth status)
     * @param response HTTP response
     * @param jwtToken JWT token value
     */
    public void setAuthenticationCookies(HttpServletResponse response, String jwtToken) {
        Cookie jwtCookie = createJwtCookie(jwtToken, JWT_EXPIRY_SECONDS);
        Cookie authStatusCookie = createAuthStatusCookie("true");

        response.addCookie(jwtCookie);
        response.addCookie(authStatusCookie);
    }
}
