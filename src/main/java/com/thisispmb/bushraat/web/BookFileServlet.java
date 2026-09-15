package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.repository.BookRepository;
import com.thisispmb.bushraat.storage.StorageObject;
import com.thisispmb.bushraat.storage.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@WebServlet("/book-file/*")
public class BookFileServlet extends HttpServlet {
    private final BookRepository bookRepository = new BookRepository();
    private final StorageService storage = StorageService.create();

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

        long bookId;
        try {
            bookId = Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Book book = bookRepository.findById(bookId);

            if (book == null || book.getFilePath() == null || book.getFilePath().isBlank()) {
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

            long size = storage.size(book.getFilePath());
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Disposition", "inline; filename=\"book.pdf\"");
            response.setContentType("application/pdf");
            response.setHeader("Cache-Control", "private, max-age=3600");

            String rangeHeader = request.getHeader("Range");
            ByteRange range = parseRange(rangeHeader, size);

            if (range == null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLengthLong(size);
                stream(storage.open(book.getFilePath()), response.getOutputStream());
                return;
            }

            if (range.invalid()) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + size);
                return;
            }

            long length = range.end() - range.start() + 1;
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader(
                    "Content-Range",
                    "bytes " + range.start() + "-" + range.end() + "/" + size
            );
            response.setContentLengthLong(length);

            stream(
                    storage.openRange(book.getFilePath(), range.start(), length),
                    response.getOutputStream()
            );
        } catch (java.io.FileNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            if (e.statusCode() == HttpServletResponse.SC_NOT_FOUND) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            throw new ServletException("Unable to load book file.", e);
        } catch (Exception e) {
            throw new ServletException("Unable to load book file.", e);
        }
    }

    private void stream(StorageObject object, OutputStream output) throws Exception {
        try (object; InputStream input = object.stream(); output) {
            input.transferTo(output);
        }
    }

    private ByteRange parseRange(String header, long size) {
        if (header == null || header.isBlank()) {
            return null;
        }

        if (!header.startsWith("bytes=") || header.substring(6).contains(",")) {
            return ByteRange.invalidRange();
        }

        String value = header.substring(6).trim();
        int dash = value.indexOf('-');
        if (dash < 0) {
            return ByteRange.invalidRange();
        }

        try {
            String startText = value.substring(0, dash).trim();
            String endText = value.substring(dash + 1).trim();

            long start;
            long end;

            if (startText.isEmpty()) {
                long suffixLength = Long.parseLong(endText);
                if (suffixLength <= 0) {
                    return ByteRange.invalidRange();
                }

                suffixLength = Math.min(suffixLength, size);
                start = size - suffixLength;
                end = size - 1;
            } else {
                start = Long.parseLong(startText);
                if (start < 0 || start >= size) {
                    return ByteRange.invalidRange();
                }

                end = endText.isEmpty() ? size - 1 : Long.parseLong(endText);
                if (end < start) {
                    return ByteRange.invalidRange();
                }

                end = Math.min(end, size - 1);
            }

            return new ByteRange(start, end, false);
        } catch (NumberFormatException e) {
            return ByteRange.invalidRange();
        }
    }

    private record ByteRange(long start, long end, boolean invalid) {
        private static ByteRange invalidRange() {
            return new ByteRange(0, 0, true);
        }
    }
}
