package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.repository.ReadingProgressRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/progress/clear")
public class ClearProgressServlet extends HttpServlet {
    private final ReadingProgressRepository progressRepository =
            new ReadingProgressRepository();

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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Book ID is required.");
            return;
        }

        try {
            Long bookId = Long.parseLong(bookIdParameter);
            Long userId = (Long) session.getAttribute("userId");

            progressRepository.delete(userId, bookId);

            response.sendRedirect(request.getContextPath() + "/continue-reading");

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID.");

        } catch (Exception e) {
            throw new ServletException("Unable to clear reading progress.", e);
        }
    }
}