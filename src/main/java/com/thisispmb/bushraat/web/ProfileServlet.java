package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.User;
import com.thisispmb.bushraat.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends ThymeleafServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("user", user);
        request.setAttribute("page", "profile");

        render("profile", request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User updatedUser = authService.updateProfile(
                    userId,
                    name,
                    email,
                    password
            );

            session.setAttribute("user", updatedUser);
            session.setAttribute("userId", updatedUser.getId());
            session.setAttribute("role", updatedUser.getRole());

            response.sendRedirect(request.getContextPath() + "/profile?updated=true");

        } catch (IllegalArgumentException e) {
            User currentUser = (User) session.getAttribute("user");

            if (currentUser != null) {
                currentUser.setName(name);
                currentUser.setEmail(email);
            }

            request.setAttribute("user", currentUser);
            request.setAttribute("error", e.getMessage());
            request.setAttribute("page", "profile");

            render("profile", request, response);

        } catch (Exception e) {
            throw new ServletException("Unable to update profile.", e);
        }
    }
}
