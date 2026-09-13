package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.repository.BookRepository;
import com.thisispmb.bushraat.util.StorageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/book-file/*")
public class BookFileServlet extends HttpServlet {

    private final BookRepository bookRepository = new BookRepository();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String idText = pathInfo.substring(1);

        Long bookId;

        try {
            bookId = Long.parseLong(idText);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Book book = bookRepository.findById(bookId);

            if (book == null
                    || book.getFilePath() == null
                    || book.getFilePath().isBlank()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Path file = StorageUtil.resolveStoredPath(book.getFilePath());

            if (!Files.exists(file) || !Files.isRegularFile(file)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (!"PDF".equalsIgnoreCase(book.getFileType())) {
                response.sendError(
                        HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                        "Only PDF books are supported."
                );
                return;
            }

            String contentType = "application/pdf";

            response.setContentType(contentType);
            response.setContentLengthLong(Files.size(file));
            response.setHeader("Content-Disposition",
                    "inline; filename=\"book\""
            );

            try (InputStream input = Files.newInputStream(file);
                 OutputStream output = response.getOutputStream()) {
                input.transferTo(output);
            }
        } catch (Exception e) {
            throw new ServletException("Unable to load book file.", e);
        }
    }
}
