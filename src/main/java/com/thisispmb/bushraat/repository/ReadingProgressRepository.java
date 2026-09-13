package com.thisispmb.bushraat.repository;

import com.thisispmb.bushraat.model.Book;
import com.thisispmb.bushraat.model.Category;
import com.thisispmb.bushraat.model.ReadingProgress;
import com.thisispmb.bushraat.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReadingProgressRepository {

    public void saveOrUpdate(ReadingProgress progress) throws SQLException {

        String sql = """
                INSERT INTO reading_progress
                    (user_id, book_id, last_page_read, last_read_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (user_id, book_id)
                DO UPDATE SET
                    last_page_read = EXCLUDED.last_page_read,
                    last_read_at = CURRENT_TIMESTAMP
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, progress.getUserId());
            statement.setLong(2, progress.getBookId());
            statement.setInt(3, progress.getLastPageRead());

            statement.executeUpdate();
        }
    }

    public void delete(Long userId, Long bookId) throws SQLException {

        String sql = """
                DELETE FROM reading_progress
                WHERE user_id = ? AND book_id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, bookId);

            statement.executeUpdate();
        }
    }

    public ReadingProgress findByUserAndBook(
            Long userId,
            Long bookId) throws SQLException {

        String sql = """
                SELECT id, user_id, book_id,
                       last_page_read, last_read_at
                FROM reading_progress
                WHERE user_id = ? AND book_id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, bookId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    ReadingProgress progress = new ReadingProgress();

                    progress.setId(resultSet.getLong("id"));
                    progress.setUserId(resultSet.getLong("user_id"));
                    progress.setBookId(resultSet.getLong("book_id"));
                    progress.setLastPageRead(
                            resultSet.getInt("last_page_read")
                    );

                    Timestamp timestamp =
                            resultSet.getTimestamp("last_read_at");

                    if (timestamp != null) {
                        progress.setLastReadAt(timestamp.toLocalDateTime());
                    }

                    return progress;
                }

                return null;
            }
        }
    }

    public List<Book> findCurrentlyReading(Long userId) throws SQLException {

        String sql = """
                SELECT b.id, b.title, b.author, b.category_id,
                       c.name AS category_name, c.description AS category_description,
                       b.description, b.cover_image_path, b.file_path, b.file_type,
                       b.uploaded_at, b.updated_at, rp.last_page_read
                FROM reading_progress rp
                JOIN books b ON b.id = rp.book_id
                LEFT JOIN categories c ON c.id = b.category_id
                WHERE rp.user_id = ?
                ORDER BY rp.last_read_at DESC
                LIMIT 6
                """;

        List<Book> books = new ArrayList<>();

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)
        ) {
            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book b = new Book();

                    b.setId(rs.getLong("id"));
                    b.setTitle(rs.getString("title"));
                    b.setAuthor(rs.getString("author"));
                    b.setCategoryId(rs.getObject("category_id", Long.class));

                    if (b.getCategoryId() != null) {
                        Category cat = new Category();

                        cat.setId(b.getCategoryId());
                        cat.setName(rs.getString("category_name"));
                        cat.setDescription(rs.getString("category_description"));
                        b.setCategory(cat);
                    }

                    b.setDescription(rs.getString("description"));
                    b.setCoverImagePath(rs.getString("cover_image_path"));
                    b.setFilePath(rs.getString("file_path"));
                    b.setFileType(rs.getString("file_type"));

                    if (rs.getTimestamp("uploaded_at") != null) {
                        b.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
                    }

                    if (rs.getTimestamp("updated_at") != null) {
                        b.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    }

                    b.setLastPageRead(rs.getInt("last_page_read"));
                    b.setProgressPercent(0);

                    books.add(b);
                }
            }
        }

        return books;
    }

    public List<ReadingProgress> findByUserId(Long userId) throws SQLException {

        String sql = """
                SELECT id, user_id, book_id,
                       last_page_read, last_read_at
                FROM reading_progress
                WHERE user_id = ?
                ORDER BY last_read_at DESC
                """;

        List<ReadingProgress> progressList = new ArrayList<>();

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ReadingProgress progress = new ReadingProgress();

                    progress.setId(resultSet.getLong("id"));
                    progress.setUserId(resultSet.getLong("user_id"));
                    progress.setBookId(resultSet.getLong("book_id"));
                    progress.setLastPageRead(resultSet.getInt("last_page_read"));

                    Timestamp timestamp = resultSet.getTimestamp("last_read_at");

                    if (timestamp != null) {
                        progress.setLastReadAt(timestamp.toLocalDateTime());
                    }

                    progressList.add(progress);
                }
            }
        }

        return progressList;
    }
}
