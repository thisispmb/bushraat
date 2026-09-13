package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.repository.FavoriteRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/saved")
public class SavedBooksServlet extends ThymeleafServlet {

    private final FavoriteRepository favoriteRepository =
            new FavoriteRepository();

    @Override
    protected void doGet(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            Long userId = (Long) session.getAttribute("userId");

            request.setAttribute("savedBooks", favoriteRepository.findBooksByUserId(userId));
            request.setAttribute("page", "saved");

            render("saved-books", request, response);
            
        } catch (Exception e) {
            throw new ServletException("Unable to load saved books.", e);
        }
    }
}
