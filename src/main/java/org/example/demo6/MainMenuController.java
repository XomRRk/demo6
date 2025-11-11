package org.example.demo6;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.sql.*;

public class MainMenuController {

    @FXML private Button startButton;
    @FXML private ComboBox<String> switchTypeComboBox;
    @FXML private TextField serialNumberField;
    @FXML private Button settingsButton;
    @FXML private Button historyButton;
    @FXML private Menu userMenu;

    @FXML
    private void initialize() {


        if (!SessionManager.isAuthenticated()) {
            Platform.runLater(this::showLoginDialog);
        }
        loadUserLastName();
        checkDatabaseConnection();
        loadSwitchTypes();
        System.out.println("MainMenuController: Initializing...");
        checkDatabaseConnection();
        loadSwitchTypes();

    }
    /**
     * Метод для показа модального окна авторизации.
     * Ожидает, пока пользователь не войдёт или не закроет окно.
     */
    private void showLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo6/loginScreen.fxml"));
            Parent loginRoot = loader.load();

            Stage loginStage = new Stage();
            loginStage.initModality(Modality.APPLICATION_MODAL);  // Модальное окно — блокирует главное меню
            loginStage.setTitle("Авторизация");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.setResizable(false);
            loginStage.showAndWait();  // Ожидаем закрытия окна

            // После закрытия проверяем, вошёл ли пользователь
            if (!SessionManager.isAuthenticated()) {
                Platform.exit(); // Если не авторизовался — закрываем приложение
            } else {
                loadUserLastName(); // Если вошёл — загружаем фамилию
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }


    private void loadUserLastName() {
        String login = SessionManager.getLogin();

        if (login == null || login.isEmpty()) {
            userMenu.setText("Гость"); // Если логина нет, отображаем гостя
            return;
        }

        String sql = "SELECT last_name FROM operators WHERE login = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String lastName = rs.getString("last_name");
                if (lastName != null && !lastName.isEmpty()) {
                    userMenu.setText(lastName); // Отображаем фамилию
                } else {
                    userMenu.setText(login); // Если фамилии нет, отображаем логин
                }
            } else {
                userMenu.setText(login); // Если пользователя нет в базе, отображаем логин
            }

        } catch (SQLException e) {
            e.printStackTrace();
            userMenu.setText(login); // При ошибке отображаем логин
        }
    }

    private void loadSwitchTypes() {
        ObservableList<String> types = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT type FROM micro_switches ORDER BY type ASC;";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                types.add(rs.getString("type"));
            }

            if (types.isEmpty()) {
                System.out.println("MainMenuController: No switch types found in the database.");
                switchTypeComboBox.setPlaceholder(new Label("Типы не найдены в БД"));
            } else {
                switchTypeComboBox.setItems(types);
            }

        } catch (SQLException e) {
            System.err.println("MainMenuController: SQL Error loading switch types: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Ошибка загрузки данных", "Не удалось загрузить типы микропереключателей:\n" + e.getMessage());
            switchTypeComboBox.setPlaceholder(new Label("Ошибка загрузки типов"));
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void checkDatabaseConnection() {
        try (Connection connection = DatabaseConnector.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("MainMenuController: Connected to the database!");
            } else {
                System.err.println("MainMenuController: Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.err.println("MainMenuController: SQL connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStartButton() {
        String selectedSwitchType = switchTypeComboBox.getValue();
        String serialNumber = serialNumberField.getText();

        if (selectedSwitchType == null || selectedSwitchType.isEmpty()) {
            showErrorAlert("Ошибка ввода", "Пожалуйста, выберите тип микропереключателя.");
            return;
        }
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            showErrorAlert("Ошибка ввода", "Пожалуйста, введите серийный номер.");
            return;
        }

        GlobalContext.setCurrentSwitchType(selectedSwitchType);
        GlobalContext.setCurrentSerialNumber(serialNumber);

        try {
            String fxmlPath = "/org/example/demo6/hello-view.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent helloScreen = loader.load();

            // Получаем текущую сцену и меняем корневой элемент (root)
            startButton.getScene().setRoot(helloScreen);

        } catch (IOException e) {
            System.err.println("MainMenuController: IOException: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Ошибка загрузки", "Не удалось загрузить интерфейс.\nПодробности в консоли.");
        } catch (Exception e) {
            System.err.println("MainMenuController: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Непредвиденная ошибка", "Ошибка при переходе на следующий экран.\nПодробности в консоли.");
        }
    }




    @FXML
    private void handleSettingsButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo6/settings-screen.fxml"));
            Parent settingsScreen = loader.load();
            settingsButton.getScene().setRoot(settingsScreen);
        } catch (IOException e) {
            showErrorAlert("Ошибка загрузки экрана", "Не удалось загрузить экран настроек.");
        }
    }

    @FXML
    private void handleHistoryButton() {
        loadScene("/org/example/demo6/historyScreen.fxml");
    }

    @FXML
    private void handleHistoryMenuAction() {
        loadScene("/org/example/demo6/historyScreen.fxml");
    }
    @FXML
    private void handleLogoutButton() {
        SessionManager.logout();
        showLoginDialog();
    }


    /**
     * Универсальный метод загрузки сцены по FXML и замены Stage.setScene(...)
     */
    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
            } else {
                System.err.println("MainMenuController: Stage is null. Can't set scene.");
                showErrorAlert("Ошибка", "Не удалось определить главное окно для загрузки сцены.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Ошибка загрузки", "Не удалось загрузить интерфейс: " + fxmlPath);
        }
    }

    /**
     * Возвращает Stage текущего окна через любую известную кнопку
     */
    private Stage getCurrentStage() {
        if (startButton != null && startButton.getScene() != null) {
            return (Stage) startButton.getScene().getWindow();
        } else if (settingsButton != null && settingsButton.getScene() != null) {
            return (Stage) settingsButton.getScene().getWindow();
        } else if (historyButton != null && historyButton.getScene() != null) {
            return (Stage) historyButton.getScene().getWindow();
        } else {
            return null;
        }
    }

}
