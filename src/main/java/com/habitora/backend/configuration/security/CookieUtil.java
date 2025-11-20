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

        boolean prod = isProd();

        // En producción: cookies cross-site → SameSite=None + Secure=true (HTTPS)
        // En local: SameSite=Lax + Secure=false (para funcionar en http://localhost)
        cookie.setSecure(prod);
        cookie.setAttribute("SameSite", prod ? "None" : "Lax");

        cookie.setMaxAge(maxAgeSeconds);

        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        boolean prod = isProd();
        cookie.setSecure(prod);
        cookie.setAttribute("SameSite", prod ? "None" : "Lax");

        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
