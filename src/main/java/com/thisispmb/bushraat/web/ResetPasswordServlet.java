package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.repository.PasswordResetRepository;
import com.thisispmb.bushraat.repository.UserRepository;
import com.thisispmb.bushraat.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends ThymeleafServlet {
    private final PasswordResetRepository resets = new PasswordResetRepository();
    private final UserRepository users = new UserRepository();

    @Override
    protected void doGet(
            HttpServletRequest r, HttpServletResponse s
    ) throws ServletException, IOException {

        r.setAttribute("token", r.getParameter("token"));
        render("reset-password", r, s);
    }

    @Override
    protected void doPost(
            HttpServletRequest r, HttpServletResponse s
    ) throws ServletException, IOException {

        String token =
                r.getParameter("token"), p = r.getParameter("password");

        try {
            if (p == null || p.length() < 8 || p.length() > 72) {
                throw new IllegalArgumentException("Password must be between 8 and 72 characters.");
            }

            Long id = resets.consume(token);

            if (id == null) {
                throw new IllegalArgumentException("This reset link is invalid or expired.");
            }

            users.updatePassword(id, PasswordUtil.hash(p));
            s.sendRedirect(r.getContextPath() + "/login?reset=true");

        } catch (IllegalArgumentException e) {
            r.setAttribute("error", e.getMessage());
            r.setAttribute("token", token);

            render("reset-password", r, s);
            
        } catch (Exception e) {
            throw new ServletException("Unable to reset password.", e);
        }
    }
}
