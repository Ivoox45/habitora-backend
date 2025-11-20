package com.habitora.backend.configuration.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${app.env:local}")
    private String environment; // local | prod

    private boolean isProd() {
        return environment.equalsIgnoreCase("prod");
    }

    public void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");

        cookie.setHttpOnly(true);
        cookie.setSecure(isProd()); // 🔥 Secure solo en prod (HTTPS)

        cookie.setMaxAge(maxAgeSeconds);

        // PROTECCIÓN CONTRA CSRF Y ATAQUES
        cookie.setAttribute("SameSite", isProd() ? "Strict" : "Lax");

        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(isProd());
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", isProd() ? "Strict" : "Lax");

        response.addCookie(cookie);
    }

    public String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
