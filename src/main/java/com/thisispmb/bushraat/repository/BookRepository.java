package com.thisispmb.bushraat.repository;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.model.Category;
import com.thisispmb.bushraat.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookRepository {

    public List<Book> findAll() throws SQLException {

        String sql = """
                SELECT b.id,
                       b.title,
                       b.author,
                       b.category_id,
                       c.name AS category_name,
                       c.description AS category_description,
                       b.description,
                       b.cover_image_path,
                       b.file_path,
                       b.file_type,
                       b.uploaded_at,
                       b.updated_at
                FROM books b
                LEFT JOIN categories c
                       ON b.category_id = c.id
                ORDER BY b.title
                """;

        List<Book> books = new ArrayList<>();

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                books.add(mapRow(resultSet));
            }
        }

        return books;
    }

    private Book mapRow(ResultSet resultSet) throws SQLException {

        Book book = new Book();

        book.setId(resultSet.getLong("id"));
        book.setTitle(resultSet.getString("title"));
        book.setAuthor(resultSet.getString("author"));

        Long categoryId =
                resultSet.getObject("category_id", Long.class);

        book.setCategoryId(categoryId);

        if (categoryId != null) {
            Category category = new Category();

            category.setId(categoryId);
            category.setName(resultSet.getString("category_name"));
            category.setDescription(resultSet.getString("category_description"));

            book.setCategory(category);
        }

        book.setDescription(resultSet.getString("description"));
        book.setCoverImagePath(resultSet.getString("cover_image_path"));
        book.setFilePath(resultSet.getString("file_path"));
        book.setFileType(resultSet.getString("file_type"));

        if (resultSet.getTimestamp("uploaded_at") != null) {
            book.setUploadedAt(resultSet.getTimestamp("uploaded_at").toLocalDateTime());
        }

        if (resultSet.getTimestamp("updated_at") != null) {
            book.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        }

        return book;
    }

    public Book findById(Long id) throws SQLException {

        String sql = """
                SELECT b.id,
                       b.title,
                       b.author,
                       b.category_id,
                       c.name AS category_name,
                       c.description AS category_description,
                       b.description,
                       b.cover_image_path,
                       b.file_path,
                       b.file_type,
                       b.uploaded_at,
                       b.updated_at
                FROM books b
                LEFT JOIN categories c
                    ON b.category_id = c.id
                WHERE b.id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }

                return null;
            }
        }
    }

    public List<Book> search(String keyword) throws SQLException {

        String sql = """
                SELECT b.id,
                       b.title,
                       b.author,
                       b.category_id,
                       c.name AS category_name,
                       c.description AS category_description,
                       b.description,
                       b.cover_image_path,
                       b.file_path,
                       b.file_type,
                       b.uploaded_at,
                       b.updated_at
                FROM books b
                LEFT JOIN categories c
                       ON b.category_id = c.id
                WHERE LOWER(b.title) LIKE LOWER(?)
                   OR LOWER(b.author) LIKE LOWER(?)
                   OR LOWER(c.name) LIKE LOWER(?)
                ORDER BY b.title
                """;

        List<Book> books = new ArrayList<>();

        String searchPattern = "%" + keyword.trim() + "%";

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapRow(resultSet));
                }
            }
        }

        return books;
    }

    public void save(Book book) throws SQLException {

        String sql = """
                INSERT INTO books (
                    title,
                    author,
                    category_id,
                    description,
                    cover_image_path,
                    file_path,
                    file_type
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id, uploaded_at, updated_at
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());

            if (book.getCategoryId() != null) {
                statement.setLong(3, book.getCategoryId());
            } else {
                statement.setNull(3, java.sql.Types.BIGINT);
            }

            statement.setString(4, book.getDescription());
            statement.setString(5, book.getCoverImagePath());
            statement.setString(6, book.getFilePath());
            statement.setString(7, book.getFileType());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    book.setId(resultSet.getLong("id"));

                    if (resultSet.getTimestamp("uploaded_at") != null) {
                        book.setUploadedAt(
                                resultSet.getTimestamp("uploaded_at")
                                        .toLocalDateTime()
                        );
                    }

                    if (resultSet.getTimestamp("updated_at") != null) {
                        book.setUpdatedAt(
                                resultSet.getTimestamp("updated_at")
                                        .toLocalDateTime()
                        );
                    }
                }
            }
        }
    }

    public void update(Book book) throws SQLException {

        String sql = """
                UPDATE books
                SET title = ?,
                    author = ?,
                    category_id = ?,
                    description = ?,
                    cover_image_path = ?,
                    file_path = ?,
                    file_type = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());

            if (book.getCategoryId() != null) {
                statement.setLong(3, book.getCategoryId());
            } else {
                statement.setNull(3, java.sql.Types.BIGINT);
            }

            statement.setString(4, book.getDescription());
            statement.setString(5, book.getCoverImagePath());
            statement.setString(6, book.getFilePath());
            statement.setString(7, book.getFileType());
            statement.setLong(8, book.getId());
            
            statement.executeUpdate();
        }
    }

    public void delete(Long bookId) throws SQLException {

        String sql = """
                DELETE FROM books
                WHERE id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, bookId);
            statement.executeUpdate();
        }
    }
}
