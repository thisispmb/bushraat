package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.repository.ReadingProgressRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/continue-reading")
public class ContinueReadingServlet extends ThymeleafServlet {

    private final ReadingProgressRepository progressRepository =
            new ReadingProgressRepository();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            Long userId = (Long) session.getAttribute("userId");

            request.setAttribute(
                    "readingBooks",
                    progressRepository.findCurrentlyReading(userId)
            );

            request.setAttribute("page", "continue");
            render("continue-reading", request, response);

        } catch (Exception e) {
            throw new ServletException("Unable to load continue reading.", e);
        }
    }
}
