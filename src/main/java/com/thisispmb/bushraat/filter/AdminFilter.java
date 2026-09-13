package com.thisispmb.bushraat.filter;

import com.thisispmb.bushraat.security.Authorization;
import com.thisispmb.bushraat.security.Role;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/admin", "/admin/*"})
public class AdminFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        try {
            Role role = Authorization.currentRole(httpRequest);

            if (role == null) {
                session.invalidate();
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
                return;
            }

            // Always use the current database role, not a stale session role.
            session.setAttribute("role", role.name());

            if (!role.canAccessAdmin()) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to verify administrator privileges.", e);
        }
    }
}
