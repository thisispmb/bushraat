package com.thisispmb.bushraat.repository;

import com.thisispmb.bushraat.security.Role;
import com.thisispmb.bushraat.util.Db;

import java.sql.*;

public class AdminRepository {

    public int countMembers() throws SQLException {
        return count("SELECT COUNT(*) FROM users");
    }

    public int countTotalSaves() throws SQLException {
        return count("SELECT COUNT(*) FROM favorites");
    }

    public int countSuperAdmins() throws SQLException {
        return count("SELECT COUNT(*) FROM users WHERE role = 'SUPER_ADMIN'");
    }

    public Role findRole(Long userId) throws SQLException {
        String sql = "SELECT role FROM users WHERE id = ?";

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return null;
                return Role.from(resultSet.getString("role"));
            }
        }
    }

    public void updateMemberRole(Long userId, Role role)
            throws SQLException {
        changeRoleSafely(userId, role);
    }

    public void deleteMember(Long userId) throws SQLException {
        String selectSql = "SELECT role FROM users WHERE id = ? FOR UPDATE";
        String deleteSql = "DELETE FROM users WHERE id = ?";

        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);

            try {
                lockSuperAdmins(connection);

                Role role;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {

                    statement.setLong(1, userId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalArgumentException("User not found.");
                        }

                        role = Role.from(resultSet.getString("role"));
                    }
                }

                if (role == Role.SUPER_ADMIN) {
                    if (countSuperAdmins(connection) <= 1) {
                        throw new IllegalArgumentException(
                                "At least one Super Administrator must remain."
                        );
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteSql)) {

                    statement.setLong(1, userId);
                    statement.executeUpdate();
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                if (e instanceof IllegalArgumentException illegalArgumentException) {
                    throw illegalArgumentException;
                }

                throw new SQLException("Unable to delete user.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void changeRoleSafely(Long userId, Role newRole)
            throws SQLException {

        if (newRole == null) {
            throw new IllegalArgumentException("Invalid role.");
        }

        String selectSql =
                "SELECT role FROM users WHERE id = ? FOR UPDATE";

        String updateSql =
                "UPDATE users SET role = ? WHERE id = ?";

        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);

            try {
                lockSuperAdmins(connection);

                Role currentRole;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {

                    statement.setLong(1, userId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalArgumentException("User not found.");
                        }

                        currentRole =
                                Role.from(resultSet.getString("role"));
                    }
                }

                if (currentRole == Role.SUPER_ADMIN
                        && newRole != Role.SUPER_ADMIN
                        && countSuperAdmins(connection) <= 1) {
                    throw new IllegalArgumentException(
                            "At least one Super Administrator must remain."
                    );
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {

                    statement.setString(1, newRole.name());
                    statement.setLong(2, userId);
                    statement.executeUpdate();
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                if (e instanceof IllegalArgumentException illegalArgumentException) {
                    throw illegalArgumentException;
                }

                throw new SQLException("Unable to change user role.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void lockSuperAdmins(Connection connection)
            throws SQLException {

        String sql = "SELECT id FROM users WHERE role = 'SUPER_ADMIN' FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                // Row locks are held until this transaction completes.
            }
        }
    }

    private int countSuperAdmins(Connection connection)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM users WHERE role = 'SUPER_ADMIN'";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
