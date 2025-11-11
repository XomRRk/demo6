    package org.example.demo6.util;

import org.example.demo6.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserUtils {
    public static String getLastNameByLogin(String login) {
        String lastName = login; // fallback
        String sql = "SELECT last_name FROM operators WHERE login = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dbLastName = rs.getString("last_name");
                if (dbLastName != null && !dbLastName.isEmpty()) {
                    lastName = dbLastName;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastName;
    }
}