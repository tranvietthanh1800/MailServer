package com.mailserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mailserver.database.DatabaseConnection;
import com.mailserver.model.User;

public class UserDAO {

    
    public boolean register(User user) {

        String sql = """
                INSERT INTO users(username, password, email)
                VALUES (?, ?, ?)
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    public User login(String username, String password) {

        String sql = """
                SELECT *
                FROM users
                WHERE username = ?
                AND password = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }

    public String getEmailByUsername(String username) {

    String sql = "SELECT email FROM users WHERE username = ?";

    try (
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql)
    ) {

        statement.setString(1, username);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getString("email");
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return null;
}
}