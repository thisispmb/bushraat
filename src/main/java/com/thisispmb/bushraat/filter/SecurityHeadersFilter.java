package com.thisispmb.bushraat.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// Adds conservative browser security headers
// to every application response.
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletResponse http = (HttpServletResponse) response;

        http.setHeader("X-Content-Type-Options", "nosniff");
        http.setHeader("X-Frame-Options", "SAMEORIGIN");
        http.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        http.setHeader(
                "Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        chain.doFilter(request, response);
    }
}
