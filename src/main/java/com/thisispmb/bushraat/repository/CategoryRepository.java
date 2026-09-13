package com.thisispmb.bushraat.repository;

import com.thisispmb.bushraat.model.Category;
import com.thisispmb.bushraat.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    public List<Category> findAll() throws SQLException {

        String sql = """
                SELECT id, name, description
                FROM categories
                ORDER BY name
                """;

        List<Category> categories = new ArrayList<>();

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Category category = new Category();

                category.setId(resultSet.getLong("id"));
                category.setName(resultSet.getString("name"));
                category.setDescription(resultSet.getString("description"));

                categories.add(category);
            }
        }

        return categories;
    }

    public Category findById(Long id) throws SQLException {

        String sql = """
                SELECT id, name, description
                FROM categories
                WHERE id = ?
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Category category = new Category();

                    category.setId(resultSet.getLong("id"));
                    category.setName(resultSet.getString("name"));
                    category.setDescription(resultSet.getString("description"));

                    return category;
                }

                return null;
            }
        }
    }

    public void save(Category category) throws SQLException {

        String sql = """
                INSERT INTO categories (
                    name,
                    description
                )
                VALUES (?, ?)
                RETURNING id
                """;

        try (Connection connection = Db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    category.setId(
                            resultSet.getLong("id")
                    );
                }
            }
        }
    }
}
