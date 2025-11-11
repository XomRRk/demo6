package org.example.demo6;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Поля не должны быть пустыми");
            return;
        }

        String hash = hashPassword(password);

        String sql = "SELECT * FROM operators WHERE login = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, hash);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                SessionManager.setUser(login, role);
                ((Stage) loginField.getScene().getWindow()).close();
            } else {
                showError("Неверный логин или пароль");
            }

        } catch (Exception e) {
            showError("Ошибка подключения к базе: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}