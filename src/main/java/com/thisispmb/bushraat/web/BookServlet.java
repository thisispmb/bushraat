package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.repository.BookRepository;
import com.thisispmb.bushraat.repository.FavoriteRepository;
import com.thisispmb.bushraat.storage.StorageObject;
import com.thisispmb.bushraat.storage.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@WebServlet(urlPatterns = {
        "/books",
        "/books/*"
})
public class BookServlet extends ThymeleafServlet {
    private final BookRepository bookRepository;
    private final FavoriteRepository favoriteRepository;
    private final StorageService storage;

    public BookServlet() {
        this.bookRepository = new BookRepository();
        this.favoriteRepository = new FavoriteRepository();
        this.storage = StorageService.create();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.endsWith("/cover")) {
            try {
                serveCover(request, response, pathInfo);
            } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
                if (e.statusCode() == HttpServletResponse.SC_NOT_FOUND) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    throw new ServletException("Unable to load book cover.", e);
                }

            } catch (Exception e) {
                throw new ServletException("Unable to load book cover.", e);
            }

            return;
        }

        try {

            // /books
            if (pathInfo == null || pathInfo.equals("/")) {
                String query = request.getParameter("search");

                if (query == null || query.isBlank()) {
                    request.setAttribute("books", bookRepository.findAll());
                } else {
                    request.setAttribute("books", bookRepository.search(query.trim()));
                }

                request.setAttribute("query", query);
                render("dashboard", request, response);

                return;
            }

            // /books/{id}
            String idText = pathInfo.substring(1);

            Long bookId;

            try {
                bookId = Long.parseLong(idText);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Book book = bookRepository.findById(bookId);

            if (book == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            HttpSession session = request.getSession(false);
            Long userId = session == null ? null
                    : (Long) session.getAttribute("userId");

            boolean saved = userId != null &&
                    favoriteRepository.exists(userId, bookId);

            book.setSaved(saved);
            request.setAttribute("book", book);

            render("book-view", request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to load book.", e);
        }
    }

    private void serveCover(
            HttpServletRequest request,
            HttpServletResponse response,
            String pathInfo
    ) throws Exception {

        String idText =
                pathInfo.substring(1, pathInfo.length() - "/cover".length());

        Long bookId;

        try {
            bookId = Long.parseLong(idText);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Book book = bookRepository.findById(bookId);

        if (book == null
                || book.getCoverImagePath() == null
                || book.getCoverImagePath().isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try (StorageObject object = storage.open(book.getCoverImagePath());
             InputStream input = object.stream();
             OutputStream output = response.getOutputStream()) {

            response.setContentType(object.contentType());
            response.setContentLengthLong(object.contentLength());
            response.setHeader("Cache-Control", "private, max-age=3600");
            input.transferTo(output);

        } catch (java.io.FileNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
