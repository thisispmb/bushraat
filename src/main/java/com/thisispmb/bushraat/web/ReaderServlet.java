package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.model.ReadingProgress;
import com.thisispmb.bushraat.repository.BookRepository;
import com.thisispmb.bushraat.repository.ReadingProgressRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/read/*")
public class ReaderServlet extends ThymeleafServlet {
    private final BookRepository books = new BookRepository();
    private final ReadingProgressRepository progress =
            new ReadingProgressRepository();

    @Override
    protected void doGet(
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        Long id = parseId(request.getPathInfo());
        if (id == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Book book = books.findById(id);

            if (book == null) {
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

            Long userId = (Long) request.getSession(false).getAttribute("userId");
            ReadingProgress saved = progress.findByUserAndBook(userId, id);

            int requested = parsePositive(request.getParameter("page"), 0);
            int page = requested > 0
                    ? requested
                    : (saved == null ? 1 : Math.max(1, saved.getLastPageRead()));

            if (requested > 0) {
                save(userId, id, page);
            }

            request.setAttribute("book", book);
            request.setAttribute("pdfPage", page);
            request.setAttribute("progressPercent", 0);

            render("reader", request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to open book reader.", e);
        }
    }

    private void save(Long userId, Long bookId, int page) throws Exception {
        ReadingProgress readingProgress = new ReadingProgress();

        readingProgress.setUserId(userId);
        readingProgress.setBookId(bookId);
        readingProgress.setLastPageRead(Math.max(1, page));

        progress.saveOrUpdate(readingProgress);
    }

    private Long parseId(String path) {
        if (path == null || path.length() < 2) {
            return null;
        }

        try {
            return Long.parseLong(path.substring(1).split("/")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private int parsePositive(String value, int fallback) {
        try {
            int number = Integer.parseInt(value);
            
            return number > 0 ? number : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
