package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.User;
import com.thisispmb.bushraat.repository.PasswordResetRepository;
import com.thisispmb.bushraat.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends ThymeleafServlet {
    private final UserRepository users = new UserRepository();
    private final PasswordResetRepository resets = new PasswordResetRepository();

    @Override
    protected void doGet(
            HttpServletRequest r, HttpServletResponse s
    ) throws ServletException, IOException {

        render("forgot-password", r, s);
    }

    @Override
    protected void doPost(
            HttpServletRequest r, HttpServletResponse s
    ) throws ServletException, IOException {

        try {
            String email = r.getParameter("email");
            User u =
                    email == null ? null : users.findByEmail(email.trim().toLowerCase());

            r.setAttribute(
                    "message",
                    "If an account exists, a reset link has been generated for this local installation."
            );

            if (u != null) {
                r.setAttribute("resetLink", r.getContextPath() + "/reset-password?token=" + resets.create(u.getId()));
            }
            
            render("forgot-password", r, s);
        } catch (Exception e) {
            throw new ServletException("Unable to start password reset.", e);
        }
    }
}
