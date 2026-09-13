package com.thisispmb.bushraat.repository;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.model.Category;
import com.thisispmb.bushraat.model.Favorite;
import com.thisispmb.bushraat.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {

    public void save(Favorite favorite) throws SQLException {

        String sql = """
                INSERT INTO favorites (user_id, book_id)
                VALUES (?, ?)
                ON CONFLICT (user_id, book_id) DO NOTHING
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, favorite.getUserId());
            statement.setLong(2, favorite.getBookId());

            statement.executeUpdate();
        }
    }

    public void delete(Long userId, Long bookId) throws SQLException {

        String sql = """
                DELETE FROM favorites
                WHERE user_id = ? AND book_id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, bookId);

            statement.executeUpdate();
        }
    }

    public boolean exists(Long userId, Long bookId) throws SQLException {

        String sql = """
                SELECT 1
                FROM favorites
                WHERE user_id = ? AND book_id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, bookId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<Book> findBooksByUserId(Long userId) throws SQLException {

        String sql = """
                SELECT b.id, b.title, b.author, b.category_id,
                       c.name AS category_name,
                       c.description AS category_description,
                       b.description, b.cover_image_path,
                       b.file_path, b.file_type,
                       b.uploaded_at, b.updated_at
                FROM favorites f
                JOIN books b ON b.id = f.book_id
                LEFT JOIN categories c ON c.id = b.category_id
                WHERE f.user_id = ?
                ORDER BY f.added_at DESC
                """;

        List<Book> books = new ArrayList<>();

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Book book = new Book();
                    book.setId(resultSet.getLong("id"));
                    book.setTitle(resultSet.getString("title"));
                    book.setAuthor(resultSet.getString("author"));
                    book.setCategoryId(resultSet.getObject("category_id", Long.class));

                    if (book.getCategoryId() != null) {
                        Category category = new Category();
                        category.setId(book.getCategoryId());
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

                    book.setSaved(true);
                    books.add(book);
                }
            }
        }

        return books;
    }

    public List<Favorite> findByUserId(Long userId) throws SQLException {

        String sql = """
                SELECT id, user_id, book_id, added_at
                FROM favorites
                WHERE user_id = ?
                ORDER BY added_at DESC
                """;

        List<Favorite> favorites = new ArrayList<>();

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {

                    Favorite favorite = new Favorite();

                    favorite.setId(resultSet.getLong("id"));
                    favorite.setUserId(resultSet.getLong("user_id"));
                    favorite.setBookId(resultSet.getLong("book_id"));

                    Timestamp timestamp = resultSet.getTimestamp("added_at");

                    if (timestamp != null) {
                        favorite.setAddedAt(timestamp.toLocalDateTime());
                    }

                    favorites.add(favorite);
                }
            }
        }

        return favorites;
    }
}
