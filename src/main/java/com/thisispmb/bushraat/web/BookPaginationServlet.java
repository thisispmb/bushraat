package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.BookPage;
import com.thisispmb.bushraat.repository.BookRepository;
import com.thisispmb.bushraat.repository.FavoriteRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/dashboard/books")
public class BookPaginationServlet extends ThymeleafServlet {

    private static final int PAGE_SIZE = 20;

    private final BookRepository bookRepository;
    private final FavoriteRepository favoriteRepository;

    public BookPaginationServlet() {
        this.bookRepository = new BookRepository();
        this.favoriteRepository = new FavoriteRepository();
    }

    @Override
    protected void doGet(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String query = request.getParameter("query");
        int page = getPage(request);

        try {
            BookPage bookPage;

            if (query == null || query.isBlank()) {
                bookPage = bookRepository.findPage(page, PAGE_SIZE);
            } else {
                bookPage = bookRepository.searchPage(query.trim(), page, PAGE_SIZE);
            }

            Set<Long> savedBookIds = new HashSet<>();

            favoriteRepository.findByUserId(userId)
                    .forEach(favorite -> savedBookIds.add(favorite.getBookId()));

            request.setAttribute("books", bookPage.books());
            request.setAttribute("savedBookIds", savedBookIds);
            request.setAttribute("requestUri", request.getContextPath() + "/dashboard");
            request.setAttribute("hasMore", bookPage.hasMore());

            response.setContentType("text/html;charset=UTF-8");

            render("fragments/book-cards", request, response);

        } catch (Exception e) {
            throw new ServletException("Unable to load more books.", e);
        }
    }

    private int getPage(HttpServletRequest request) {
        String value = request.getParameter("page");

        if (value == null || value.isBlank()) {
            return 1;
        }

        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}