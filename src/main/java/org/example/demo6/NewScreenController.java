package org.example.demo6;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.example.demo6.util.UserUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class NewScreenController {

    @FXML private StackPane nameBlock;

    @FXML private StackPane paneStraight;
    @FXML private Label labelStraight;

    @FXML private StackPane paneDiff;
    @FXML private Label labelDiff;

    @FXML private StackPane paneAdd;
    @FXML private Label labelAdd;

    @FXML private StackPane paneTimeFwdHigh;
    @FXML private Label labelTimeFwdHigh;

    @FXML private StackPane paneTimeBackHigh;
    @FXML private Label labelTimeBackHigh;

    @FXML private StackPane paneTimeFwdLow;
    @FXML private Label labelTimeFwdLow;

    @FXML private StackPane paneTimeBackLow;
    @FXML private Label labelTimeBackLow;

    @FXML private StackPane paneDebounceNO;
    @FXML private Label labelDebounceNO;

    @FXML private StackPane paneDebounceNC;
    @FXML private Label labelDebounceNC;

    @FXML private StackPane paneVoltDropNO;
    @FXML private Label labelVoltDropNO;

    @FXML private StackPane paneVoltDropNC;
    @FXML private Label labelVoltDropNC;

    @FXML private StackPane paneForceFwd;
    @FXML private Label labelForceFwd;

    @FXML private StackPane paneForceBack;
    @FXML private Label labelForceBack;

    @FXML private StackPane paneGood;
    @FXML private StackPane paneBad;
    @FXML private StackPane paneError;

    @FXML private Label nameLabel;
    @FXML private Menu userMenu;

    private static final String COLOR_GOOD = "green";
    private static final String COLOR_BAD = "red";

    private final List<Label> labels = new ArrayList<>();
    private final List<StackPane> panes = new ArrayList<>();

    // Количество попыток опроса регистров
    private int pollAttempts = 0;

    // Максимальное число попыток опроса (3 раза)
    private final int maxPollAttempts = 3;

    // Таймер для опроса регистров с интервалом
    private Timeline pollingTimeline;



    private final List<Range> ranges = Arrays.asList(
            new Range(200.0, 1200.0),
            new Range(100.0, 500.0),
            new Range(800.0, null),
            new Range(null, 10000.0),
            new Range(null, 10000.0),
            new Range(null, 10000.0),
            new Range(null, 10000.0),
            new Range(null, 10000.0),
            new Range(null, 10000.0),
            new Range(null, 10000.0),
            new Range(null, 10000.0),
            new Range(100.0, 500.0),
            new Range(800.0, null)
    );

    private static class Range {
        final Double min;
        final Double max;

        Range(Double min, Double max) {
            this.min = min;
            this.max = max;
        }

        boolean inRange(double value) {
            if (min != null && value < min) return false;
            if (max != null && value > max) return false;
            return true;
        }
    }

    @FXML
    private void initialize() {
        String login = SessionManager.getLogin();
        if (login != null) {
            String lastName = UserUtils.getLastNameByLogin(login);
            if (userMenu != null) {
                userMenu.setText(lastName);
            }
        } else {
            if (userMenu != null) {
                userMenu.setText("Гость");
            }
        }
        labels.addAll(Arrays.asList(
                labelStraight, labelDiff, labelAdd,
                labelTimeFwdHigh, labelTimeBackHigh, labelTimeFwdLow, labelTimeBackLow,
                labelDebounceNO, labelDebounceNC,
                labelVoltDropNO, labelVoltDropNC,
                labelForceFwd, labelForceBack
        ));

        panes.addAll(Arrays.asList(
                paneStraight, paneDiff, paneAdd,
                paneTimeFwdHigh, paneTimeBackHigh, paneTimeFwdLow, paneTimeBackLow,
                paneDebounceNO, paneDebounceNC,
                paneVoltDropNO, paneVoltDropNC,
                paneForceFwd, paneForceBack
        ));

        nameLabel.setText(GlobalContext.getCurrentSerialNumber());

        testWithRandomValues();
        // Запускаем опрос при инициализации контроллера
        startPolling();

    }
    /**
     * Запуск таймера, который опрашивает регистры каждые 3 секунды,
     * максимум 3 раза (maxPollAttempts).
     */
    private void startPolling() {
        pollAttempts = 0;  // Сбрасываем счетчик попыток
        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(3), ev -> pollRegisters()));
        pollingTimeline.setCycleCount(maxPollAttempts);  // Повторить maxPollAttempts раз
        pollingTimeline.play();  // Запускаем таймер
    }

    /**
     * Метод опрашивает два регистра:
     * - регистр 0 для аналогового значения (обновляет labelStraight)
     * - регистр 4 для дискретных входов (обновляет соответствующие лейблы)
     * Если оба опроса успешны — таймер останавливается.
     * Если по истечении maxPollAttempts данные не получены — показывается ошибка.
     */
    private void pollRegisters() {
        pollAttempts++;  // Увеличиваем счетчик попыток

        boolean analogSuccess = false;    // Флаг успеха опроса регистра 0
        boolean discreteSuccess = false;  // Флаг успеха опроса регистра 4

        // --- Опрос регистра 0 (аналоговое значение) ---
        try {
            int[] values = ModbusService.getInstance().readInputRegisters(1, 0, 1);  // Читаем 1 регистр с адреса 0 у slave 1
            if (values.length > 0) {
                labelStraight.setText(String.valueOf(values[0]));  // Отображаем значение в labelStraight
                analogSuccess = true;  // Помечаем как успешно
            } else {
                labelStraight.setText("Нет данных");  // Если пустой ответ
            }
        } catch (Exception e) {
            // Если по максимальному числу попыток ещё не получили данных — показываем ошибку
            if (pollAttempts >= maxPollAttempts) {
                labelStraight.setText("Ошибка");
            }
            e.printStackTrace();  // Логируем ошибку для отладки
        }

        // --- Опрос регистра 4 (дискретные входы) ---
        try {
            int[] discreteValues = ModbusService.getInstance().readInputRegisters(1, 4, 1);  // Читаем 1 регистр с адреса 4
            if (discreteValues.length > 0) {
                int bits = discreteValues[0];  // Получаем 16-битное число

                // Проходим по списку лейблов, обновляя текст "1" или "0" по битам
                for (int i = 0; i < labels.size(); i++) {
                    boolean bitSet = ((bits >> i) & 1) == 1;  // Проверяем i-й бит
                    labels.get(i).setText(bitSet ? "1" : "0");
                }

                paneError.setStyle("-fx-background-color: transparent;");  // Сбрасываем ошибку

                discreteSuccess = true;  // Помечаем как успешно
            } else {
                throw new Exception("Нет данных");
            }
        } catch (Exception e) {
            if (pollAttempts >= maxPollAttempts) {
                paneError.setStyle("-fx-background-color: red;");  // Подсвечиваем ошибку красным
            }
            e.printStackTrace();
        }

        // Если оба опроса прошли успешно — останавливаем таймер опроса
        if (analogSuccess && discreteSuccess) {
            pollingTimeline.stop();
        }
    }


    public void checkAndDisplayParameters(List<Double> values) {
        if (values.size() != ranges.size()) {
            paneError.setStyle("-fx-background-color: " + COLOR_GOOD + ";");
            return;
        }

        boolean allGood = true;

        for (int i = 0; i < values.size(); i++) {
            double val = values.get(i);
            labels.get(i).setText(String.valueOf(val));

            if (ranges.get(i).inRange(val)) {
                panes.get(i).setStyle("-fx-background-color: " + COLOR_GOOD + ";");
            } else {
                panes.get(i).setStyle("-fx-background-color: " + COLOR_BAD + ";");
                allGood = false;
            }
        }

        if (allGood) {
            paneGood.setStyle("-fx-background-color: " + COLOR_GOOD + ";");
        } else {
            paneBad.setStyle("-fx-background-color: " + COLOR_BAD + ";");
        }

        saveResultsToDatabase(values);
    }

    public void testWithRandomValues() {
        List<Double> randoms = new ArrayList<>();

        // Внимательно подбираем значения в соответствии с precision:
        // direct_travel, differential_travel, additional_travel — до 99.99
        // force — до 1500, но тоже ограничим для безопасности
        // дребезг и время — до 9999.99

        randoms.add(randomDouble(10, 90));   // direct_travel
        randoms.add(randomDouble(5, 50));    // differential
        randoms.add(randomDouble(10, 70));   // additional

        randoms.add(randomDouble(100, 3000)); // timeFwdHigh
        randoms.add(randomDouble(100, 3000)); // timeBackHigh
        randoms.add(randomDouble(100, 3000)); // timeFwdLow
        randoms.add(randomDouble(100, 3000)); // timeBackLow

        randoms.add(randomDouble(50, 500));   // debounceNO
        randoms.add(randomDouble(50, 500));   // debounceNC

        randoms.add(randomDouble(10, 200));   // voltDropNO
        randoms.add(randomDouble(10, 200));   // voltDropNC

        randoms.add(randomDouble(100, 800));  // forceFwd
        randoms.add(randomDouble(100, 800));  // forceBack

        checkAndDisplayParameters(randoms);
    }

    private double randomDouble(double min, double max) {
        return Math.round((min + Math.random() * (max - min)) * 100.0) / 100.0; // округляем до 2 знаков
    }

    private void saveResultsToDatabase(List<Double> values) {
        String serial = GlobalContext.getCurrentSerialNumber();
        String type = GlobalContext.getCurrentSwitchType();

        if (serial == null || type == null) {
            System.err.println("Ошибка: Не заданы серийный номер или тип микропереключателя.");
            return;
        }

        String selectSwitchId = "SELECT id FROM micro_switches WHERE type = ?";
        String selectTestId = "SELECT id FROM tests WHERE switch_id = ? LIMIT 1";
        String insertQuery = """
            INSERT INTO test_results (
                serial_number, switch_id, test_id, operator_id,
                direct_travel, differential_travel, additional_travel,
                actuation_force_direct, actuation_force_reverse,
                no_sticking, movement_speed, voltage_drop_no, voltage_drop_nc,
                test_date, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
        """;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement switchIdStmt = conn.prepareStatement(selectSwitchId)) {

            switchIdStmt.setString(1, type);
            ResultSet switchRs = switchIdStmt.executeQuery();

            if (!switchRs.next()) {
                System.err.println("Не найден switch_id для типа: " + type);
                return;
            }

            int switchId = switchRs.getInt("id");

            int testId;
            try (PreparedStatement testStmt = conn.prepareStatement(selectTestId)) {
                testStmt.setInt(1, switchId);
                ResultSet testRs = testStmt.executeQuery();
                if (testRs.next()) {
                    testId = testRs.getInt("id");
                } else {
                    System.err.println("Не найден test_id для switch_id: " + switchId);
                    return;
                }
            }

            int operatorId = 1;
            boolean noSticking = true;
            String status = paneBad.getStyle().contains("green") ? "FAILED" : "PASSED";

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, serial);
                insertStmt.setInt(2, switchId);
                insertStmt.setInt(3, testId);
                insertStmt.setInt(4, operatorId);

                insertStmt.setDouble(5, values.get(0));
                insertStmt.setDouble(6, values.get(1));
                insertStmt.setDouble(7, values.get(2));
                insertStmt.setDouble(8, values.get(11));
                insertStmt.setDouble(9, values.get(12));

                insertStmt.setBoolean(10, noSticking);
                insertStmt.setDouble(11, 1.7);
                insertStmt.setDouble(12, values.get(9));
                insertStmt.setDouble(13, values.get(10));

                insertStmt.setString(14, status);

                insertStmt.executeUpdate();
                System.out.println("✅ Результаты успешно сохранены.");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении результатов:");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMainMenuButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo6/mainMenuScreen.fxml"));
            Parent mainMenu = loader.load();
            AnchorPane rootContainer = (AnchorPane) nameBlock.getScene().getRoot();
            rootContainer.getChildren().setAll(mainMenu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
