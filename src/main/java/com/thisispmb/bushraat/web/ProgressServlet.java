package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.model.ReadingProgress;
import com.thisispmb.bushraat.repository.BookRepository;
import com.thisispmb.bushraat.repository.ReadingProgressRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/progress")
public class ProgressServlet extends HttpServlet {
    private final ReadingProgressRepository progressRepository =
            new ReadingProgressRepository();

    private final BookRepository bookRepository =
            new BookRepository();

    @Override
    protected void doPost(
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null) {

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String bookIdParameter = request.getParameter("bookId");
        String pageParameter = request.getParameter("page");

        if (bookIdParameter == null
                || bookIdParameter.isBlank()
                || pageParameter == null
                || pageParameter.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Book ID and page are required."
            );
            return;
        }

        try {
            Long bookId = Long.parseLong(bookIdParameter);
            int page = Integer.parseInt(pageParameter);

            if (page < 1) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Page must be greater than zero."
                );
                return;
            }

            Book book = bookRepository.findById(bookId);

            if (book == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Book not found."
                );
                return;
            }

            Long userId = (Long) session.getAttribute("userId");

            ReadingProgress progress = new ReadingProgress();

            progress.setUserId(userId);
            progress.setBookId(bookId);
            progress.setLastPageRead(page);

            progressRepository.saveOrUpdate(progress);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid book ID or page."
            );

        } catch (Exception e) {
            throw new ServletException("Unable to save reading progress.", e);
        }
    }
}