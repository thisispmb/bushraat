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
            ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof Number)) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            Role role = Authorization.currentRole(req);

            if (role == null) {
                session.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            if (!role.canAccessAdmin()) {
                resp.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Administrator privileges required."
                );
                return;
            }

            // Keep the session synchronized with the database.
            session.setAttribute("role", role.name());

            chain.doFilter(request, response);

        } catch (Exception e) {
            throw new ServletException("Unable to verify administrator privileges.", e);
        }
    }
}