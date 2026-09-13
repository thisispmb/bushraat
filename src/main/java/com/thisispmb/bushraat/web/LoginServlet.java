package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.User;
import com.thisispmb.bushraat.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends ThymeleafServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        render("login", request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        // HTML field is named "username".
        // We use it as the email address.
        String email = request.getParameter("username");
        String password = request.getParameter("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            response.sendRedirect(request.getContextPath() + "/login?error=true");
            return;
        }

        try {
            User user = authService.authenticate(email, password);

            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login?error=true");
                return;
            }


            // Prevent session fixation by changing the session ID
            // after successful authentication.

            HttpSession session = request.getSession(true);
            request.changeSessionId();

            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole());

            response.sendRedirect(request.getContextPath() + "/dashboard");

        } catch (Exception e) {
            throw new ServletException("Login failed.", e);
        }
    }
}
