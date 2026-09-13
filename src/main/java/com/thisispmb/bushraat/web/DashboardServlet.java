package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.repository.BookRepository;
import com.thisispmb.bushraat.repository.FavoriteRepository;
import com.thisispmb.bushraat.repository.ReadingProgressRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/dashboard")
public class DashboardServlet extends ThymeleafServlet {

    private final BookRepository bookRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReadingProgressRepository readingProgressRepository;

    public DashboardServlet() {
        this.bookRepository = new BookRepository();
        this.favoriteRepository = new FavoriteRepository();
        this.readingProgressRepository = new ReadingProgressRepository();
    }

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

        Long userId = (Long) session.getAttribute("userId");
        String query = request.getParameter("query");

        try {
            List<Book> books;

            if (query == null || query.isBlank()) {
                books = bookRepository.findAll();
            } else {
                books = bookRepository.search(query.trim());
            }

            Set<Long> savedBookIds = new HashSet<>();

            favoriteRepository.findByUserId(userId)
                    .forEach(favorite -> savedBookIds.add(favorite.getBookId()));

            request.setAttribute("books", books);
            request.setAttribute("query", query);
            request.setAttribute("savedBookIds", savedBookIds);
            request.setAttribute("readingBooks", readingProgressRepository.findCurrentlyReading(userId));
            request.setAttribute("page", "home");

            render("dashboard", request, response);

        } catch (Exception e) {
            throw new ServletException("Unable to load dashboard.", e);
        }
    }
}
