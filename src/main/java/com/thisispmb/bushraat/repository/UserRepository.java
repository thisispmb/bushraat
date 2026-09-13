package com.thisispmb.bushraat.repository;

import com.thisispmb.bushraat.model.User;
import com.thisispmb.bushraat.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User findByEmail(String email) throws SQLException {

        String sql = """
                SELECT id, name, email, password_hash, role, created_at
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();

                    user.setId(resultSet.getLong("id"));
                    user.setName(resultSet.getString("name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPasswordHash(resultSet.getString("password_hash"));
                    user.setRole(resultSet.getString("role"));
                    user.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

                    return user;
                }

                return null;
            }
        }
    }

    public User save(User user) throws SQLException {

        String sql = """
                INSERT INTO users (name, email, password_hash, role)
                VALUES (?, ?, ?, ?)
                RETURNING id, created_at
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getLong("id"));
                    user.setCreatedAt(
                            resultSet.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }

        return user;
    }

    public User findById(Long id) throws SQLException {

        String sql = """
                SELECT id, name, email, password_hash, role, created_at
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();

                    user.setId(resultSet.getLong("id"));
                    user.setName(resultSet.getString("name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPasswordHash(resultSet.getString("password_hash"));
                    user.setRole(resultSet.getString("role"));
                    user.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

                    return user;
                }

                return null;
            }
        }
    }

    public List<User> findAll() throws SQLException {

        String sql = """
                SELECT id,
                       name,
                       email,
                       password_hash,
                       role,
                       created_at
                FROM users
                ORDER BY created_at DESC
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                User user = new User();

                user.setId(resultSet.getLong("id"));
                user.setName(resultSet.getString("name"));
                user.setEmail(resultSet.getString("email"));
                user.setPasswordHash(resultSet.getString("password_hash"));
                user.setRole(resultSet.getString("role"));

                if (resultSet.getTimestamp("created_at") != null) {
                    user.setCreatedAt(
                            resultSet.getTimestamp("created_at")
                                    .toLocalDateTime());
                }

                users.add(user);
            }
        }

        return users;
    }

    public boolean emailExistsForAnotherUser(String email, Long userId) throws SQLException {

        String sql = """
                SELECT 1
                FROM users
                WHERE email = ? AND id <> ?
                LIMIT 1
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public void updateDetails(Long userId, String name, String email) throws SQLException {

        String sql = """
                UPDATE users
                SET name = ?, email = ?
                WHERE id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setLong(3, userId);
            
            statement.executeUpdate();
        }
    }

    public void updatePassword(Long userId, String passwordHash) throws SQLException {

        String sql = """
                UPDATE users
                SET password_hash = ?
                WHERE id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setLong(2, userId);

            statement.executeUpdate();
        }
    }

}
