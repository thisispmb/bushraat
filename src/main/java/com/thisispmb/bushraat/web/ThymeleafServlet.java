package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.config.ThymeleafConfig;
import com.thisispmb.bushraat.security.Role;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

public abstract class ThymeleafServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        templateEngine =
                ThymeleafConfig.createTemplateEngine(config.getServletContext());
    }

    protected void render(
            String template, HttpServletRequest request, HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        IWebExchange exchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(request, response);

        WebContext context =
                new WebContext(exchange, request.getLocale());

        HttpSession session = request.getSession(false);

        if (session != null) {
            context.setVariable("user", session.getAttribute("user"));

            Role role = Role.from((String) session.getAttribute("role"));
            context.setVariable("role", role);
            context.setVariable("isAdmin",
                    role != null && role.canAccessAdmin()
            );
            context.setVariable("isSuperAdmin", role == Role.SUPER_ADMIN);
            context.setVariable("isLibrarian", role == Role.LIBRARIAN);
        } else {
            context.setVariable("role", null);
            context.setVariable("isAdmin", false);
            context.setVariable("isSuperAdmin", false);
            context.setVariable("isLibrarian", false);
        }

        context.setVariable("requestUri", request.getRequestURI());
        context.setVariable("contextPath", request.getContextPath());

        templateEngine.process(template, context, response.getWriter());
    }
}
