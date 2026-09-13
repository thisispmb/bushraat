package com.thisispmb.bushraat.web;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.model.Category;
import com.thisispmb.bushraat.repository.AdminRepository;
import com.thisispmb.bushraat.repository.BookRepository;
import com.thisispmb.bushraat.repository.CategoryRepository;
import com.thisispmb.bushraat.repository.UserRepository;
import com.thisispmb.bushraat.security.Authorization;
import com.thisispmb.bushraat.security.Role;
import com.thisispmb.bushraat.util.StorageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@WebServlet(urlPatterns = {"/admin", "/admin/*"})
@MultipartConfig(maxFileSize = 50L * 1024 * 1024, maxRequestSize = 60L * 1024 * 1024)
public class AdminServlet extends ThymeleafServlet {
    private final BookRepository bookRepository = new BookRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final UserRepository userRepository = new UserRepository();
    private final AdminRepository adminRepository = new AdminRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            loadDashboard(req);
            String edit = req.getParameter("edit");

            if (edit != null && !edit.isBlank()) {
                Long id = parseId(edit);
                Book book = bookRepository.findById(id);

                if (book == null) {
                    resp.sendError(404);
                    return;
                }

                req.setAttribute("editingBook", book);
            }

            req.setAttribute("page", "admin");
            render("admin/dashboard", req, resp);
        } catch (Exception e) {
            throw new ServletException("Unable to load admin dashboard.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

        if (path == null || "/".equals(path)) {
            resp.sendError(400);
            return;
        }

        try {
            if ("/categories".equals(path)) {
                createCategory(req, resp);
                return;
            }

            if ("/books".equals(path)) {
                createBook(req, resp);
                return;
            }

            if (path.matches("/books/\\d+/edit")) {
                editBook(parseId(path.substring(7, path.length() - 5)), req, resp);
                return;
            }

            if (path.matches("/books/\\d+/delete")) {
                deleteBook(parseId(path.substring(7, path.length() - 7)), req, resp);
                return;
            }

            if (path.matches("/members/\\d+/role")) {
                updateMemberRole(parseId(path.substring(9, path.length() - 5)), req, resp);
                return;
            }

            if (path.matches("/members/\\d+/delete")) {
                deleteMember(parseId(path.substring(9, path.length() - 7)), req, resp);
                return;
            }

            resp.sendError(404);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid identifier.");
        } catch (UnsupportedMediaTypeException e) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, e.getMessage());
        } catch (IllegalArgumentException e) {
            resp.sendError(400, e.getMessage());
        } catch (Exception e) {
            throw new ServletException("Admin operation failed.", e);
        }
    }

    private void createCategory(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        requireAdmin(req);

        String name = trim(req.getParameter("name"));
        String description = trimToNull(req.getParameter("description"));

        if (name == null) {
            throw new IllegalArgumentException("Category name is required.");
        }

        Category c = new Category();

        c.setName(name);
        c.setDescription(description);

        categoryRepository.save(c);

        resp.sendRedirect(req.getContextPath() + "/admin");
    }

    private void createBook(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        requireAdmin(req);

        String title = trim(req.getParameter("title")), author = trim(req.getParameter("author")), description = trimToNull(req.getParameter("description"));
        String category = trim(req.getParameter("categoryId"));
        Part file = req.getPart("file"), cover = req.getPart("cover");

        if (title == null || author == null ||
                category == null || file == null || file.getSize() == 0) {
            throw new IllegalArgumentException(
                    "Title, author, category and book file are required.");
        }

        long categoryId = Long.parseLong(category);

        if (categoryRepository.findById(categoryId) == null) {
            throw new IllegalArgumentException("Selected category does not exist.");
        }

        StoredUpload bookUpload = storeBook(file);
        StoredUpload coverUpload = cover != null && cover.getSize() > 0 ? storeCover(cover) : null;

        try {
            Book b = new Book();

            b.setTitle(title);
            b.setAuthor(author);
            b.setDescription(description);
            b.setCategoryId(categoryId);
            b.setFilePath(bookUpload.path);
            b.setFileType(bookUpload.type);
            b.setCoverImagePath(coverUpload == null ? null : coverUpload.path);

            bookRepository.save(b);
        } catch (Exception e) {
            deleteQuietly(bookUpload.file);

            if (coverUpload != null) {
                deleteQuietly(coverUpload.file);
            }

            throw e;
        }

        resp.sendRedirect(req.getContextPath() + "/admin");
    }

    private void editBook(Long id, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        requireAdmin(req);

        Book b = bookRepository.findById(id);

        if (b == null) {
            resp.sendError(404);
            return;
        }

        String title = trim(req.getParameter("title"));
        String author = trim(req.getParameter("author"));
        String category = trim(req.getParameter("categoryId"));

        if (title == null || author == null || category == null) {
            throw new IllegalArgumentException("Title, author and category are required.");
        }

        b.setTitle(title);
        b.setAuthor(author);
        b.setDescription(trimToNull(req.getParameter("description")));
        b.setCategoryId(Long.parseLong(category));

        if (categoryRepository.findById(b.getCategoryId()) == null) {
            throw new IllegalArgumentException("Selected category does not exist.");
        }

        Part file = req.getPart("file"), cover = req.getPart("cover");

        StoredUpload newBook = file != null && file.getSize() > 0 ? storeBook(file) : null;
        StoredUpload newCover = cover != null && cover.getSize() > 0 ? storeCover(cover) : null;

        String oldBook = b.getFilePath(), oldCover = b.getCoverImagePath();

        if (newBook != null) {
            b.setFilePath(newBook.path);
            b.setFileType(newBook.type);
        }

        if (newCover != null) {
            b.setCoverImagePath(newCover.path);
        }

        try {
            bookRepository.update(b);
        } catch (Exception e) {
            if (newBook != null) {
                deleteQuietly(newBook.file);
            }

            if (newCover != null) {
                deleteQuietly(newCover.file);
            }

            throw e;
        }

        if (newBook != null) {
            deleteQuietly(StorageUtil.resolveStoredPath(oldBook));
        }

        if (newCover != null) {
            deleteQuietly(StorageUtil.resolveStoredPath(oldCover));
        }

        resp.sendRedirect(req.getContextPath() + "/admin");
    }

    private void deleteBook(Long id, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        requireAdmin(req);

        Book b = bookRepository.findById(id);

        if (b == null) {
            resp.sendError(404);
            return;
        }

        bookRepository.delete(id);

        deleteQuietly(StorageUtil.resolveStoredPath(b.getFilePath()));
        deleteQuietly(StorageUtil.resolveStoredPath(b.getCoverImagePath()));

        resp.sendRedirect(req.getContextPath() + "/admin");
    }

    private void updateMemberRole(Long id, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        requireSuperAdmin(req);

        Long current = currentUserId(req);

        if (id.equals(current)) {
            throw new IllegalArgumentException("You cannot change your own role.");
        }

        String roleValue = trim(req.getParameter("role"));
        Role role = Role.from(roleValue);

        if (role == null) {
            throw new IllegalArgumentException("Invalid role.");
        }

        adminRepository.updateMemberRole(id, role);
        resp.sendRedirect(req.getContextPath() + "/admin");
    }

    private void deleteMember(Long id, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Long current = currentUserId(req);

        if (id.equals(current)) {
            throw new IllegalArgumentException("You cannot delete your own account here.");
        }

        Role actor = requireAdmin(req);
        Role target = adminRepository.findRole(id);

        if (target == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (actor == Role.LIBRARIAN && target != Role.USER) {
            throw new IllegalArgumentException(
                    "Librarians can only remove normal users."
            );
        }

        adminRepository.deleteMember(id);
        resp.sendRedirect(req.getContextPath() + "/admin");
    }

    private Role requireAdmin(HttpServletRequest req) throws Exception {
        Role role = Authorization.currentRole(req);

        if (role == null || !role.canAccessAdmin()) {
            throw new IllegalArgumentException("Administrator privileges required.");
        }

        return role;
    }

    private void requireSuperAdmin(HttpServletRequest req) throws Exception {
        if (!Authorization.isSuperAdmin(req)) {
            throw new IllegalArgumentException(
                    "Super Administrator privileges required."
            );
        }
    }

    private Long currentUserId(HttpServletRequest req) {
        Object id = req.getSession(false).getAttribute("userId");

        if (!(id instanceof Number number)) {
            throw new IllegalArgumentException("Authentication required.");
        }

        return number.longValue();
    }

    private void loadDashboard(HttpServletRequest req) throws Exception {
        List<Book> b = bookRepository.findAll();

        req.setAttribute("books", b);
        req.setAttribute("categories", categoryRepository.findAll());
        req.setAttribute("members", userRepository.findAll());
        req.setAttribute("totalSaves", adminRepository.countTotalSaves());
    }

    private Long parseId(String s) {
        return Long.parseLong(s);
    }

    private String trim(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private String trimToNull(String s) {
        return trim(s);
    }

    private StoredUpload storeBook(Part p) throws Exception {
        String n = p.getSubmittedFileName();

        if (n == null) {
            throw new IllegalArgumentException("Book file is required.");
        }

        String l = n.toLowerCase();
        String type = l.endsWith(".pdf") ? "PDF" : null;

        if (type == null) {
            throw new UnsupportedMediaTypeException("Only PDF books are supported.");
        }

        Path f = StorageUtil.books().resolve(UUID.randomUUID() + ".pdf");
        p.write(f.toString());

        return new StoredUpload("/uploads/books/" + f.getFileName(), type, f);
    }

    private StoredUpload storeCover(Part p) throws Exception {
        String n = p.getSubmittedFileName();

        if (n == null) {
            throw new IllegalArgumentException("Invalid cover image.");
        }

        String l = n.toLowerCase();
        String ext = l.endsWith(".jpg") || l.endsWith(".jpeg")
                ? ".jpg" : l.endsWith(".png") ? ".png" : l.endsWith(".webp")
                ? ".webp" : null;

        if (ext == null) {
            throw new IllegalArgumentException("Cover must be JPG, PNG or WEBP.");
        }

        Path f = StorageUtil.covers().resolve(UUID.randomUUID() + ext);
        p.write(f.toString());
        
        return new StoredUpload("/uploads/covers/" + f.getFileName(), null, f);
    }

    private void deleteQuietly(Path p) {
        if (p != null) try {
            Files.deleteIfExists(p);
        } catch (Exception ignored) {
        }
    }

    private static final class UnsupportedMediaTypeException extends Exception {
        private UnsupportedMediaTypeException(String message) {
            super(message);
        }
    }

    private record StoredUpload(String path, String type, Path file) {
    }
}
