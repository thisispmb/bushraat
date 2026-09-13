package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends ThymeleafServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        render("register", request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Keep the entered values so the form can be repopulated
        // if validation fails.
        request.setAttribute("name", name);
        request.setAttribute("email", email);

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {

            request.setAttribute("error", "Please fill in all fields.");

            render("register", request, response);
            return;
        }

        try {
            authService.register(name, email, password);

            response.sendRedirect(
                    request.getContextPath()
                            + "/login?registered=true"
            );

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());

            render("register", request, response);

        } catch (Exception e) {
            throw new ServletException("Registration failed.", e);
        }
    }
}
