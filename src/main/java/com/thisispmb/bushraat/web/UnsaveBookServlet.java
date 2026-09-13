package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.repository.FavoriteRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/unsave")
public class UnsaveBookServlet extends HttpServlet {

    private final FavoriteRepository favoriteRepository =
            new FavoriteRepository();

    @Override
    protected void doPost(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String bookIdParameter = request.getParameter("bookId");

        if (bookIdParameter == null || bookIdParameter.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            Long bookId = Long.parseLong(bookIdParameter);

            favoriteRepository.delete(userId, bookId);

            String redirect = request.getParameter("redirect");

            if (redirect == null || redirect.isBlank() || !redirect.startsWith("/")) {
                redirect = "/dashboard";
            }

            response.sendRedirect(request.getContextPath() + redirect);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } catch (Exception e) {
            throw new ServletException("Unable to remove saved book.", e);
        }
    }
}
