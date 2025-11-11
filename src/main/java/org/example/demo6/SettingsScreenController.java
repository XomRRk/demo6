package org.example.demo6;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.TextField;

import java.io.IOException;

public class SettingsScreenController {

    // Левые поля (Авто режим)
    @FXML private TextField avgCyclesField;
    @FXML private TextField maxCyclesField;
    @FXML private TextField touchWeightField;
    @FXML private TextField stopWeightField;

    // Выключатель-время
    @FXML private TextField maxAvgOnField;
    @FXML private TextField maxAvgOffField;
    @FXML private TextField maxOnField;
    @FXML private TextField maxOffField;
    @FXML private TextField debounceNCField;
    @FXML private TextField debounceNOField;

    // Правые поля (Выключатель-ход)
    @FXML private TextField maxDirectField;
    @FXML private TextField minDirectField;
    @FXML private TextField maxAdditionalField;
    @FXML private TextField minAdditionalField;
    @FXML private TextField maxDiffField;
    @FXML private TextField minDiffField;

    // Выключатель-напряжение
    @FXML private TextField maxNCField;
    @FXML private TextField minNOField;

    // Кнопки
    @FXML private Button saveParametersButton;
    @FXML private Button backButton;
    @FXML
    private Menu userMenu;  // если в меню

    @FXML
    private void initialize() {
        saveParametersButton.setOnAction(event -> saveParameters());
    }

    private int buildBitmaskFromFields() {
        int bitmask = 0;

        // Собираем все 16 полей
        TextField[] fields = new TextField[] {
                avgCyclesField,
                maxCyclesField,
                touchWeightField,
                stopWeightField,
                maxAvgOnField,
                maxAvgOffField,
                maxOnField,
                maxOffField,
                debounceNCField,
                debounceNOField,
                maxDirectField,
                minDirectField,
                maxAdditionalField,
                minAdditionalField,
                maxDiffField,
                minDiffField  // добавлено 16-е поле
        };

        for (int i = 0; i < fields.length; i++) {
            String text = fields[i].getText().trim();
            int bit = "1".equals(text) ? 1 : 0;
            bitmask |= (bit << (15 - i));
        }

        return bitmask;
    }


    private void saveParameters() {
        System.out.println("Сохраняем параметры:");
        System.out.println("Циклов ср. вр.: " + avgCyclesField.getText());
        System.out.println("Циклов макс. вр.: " + maxCyclesField.getText());
        System.out.println("Вес касания: " + touchWeightField.getText());
        System.out.println("Вес упора: " + stopWeightField.getText());

        System.out.println("Макс ср. вкл.: " + maxAvgOnField.getText());
        System.out.println("Макс ср. выкл.: " + maxAvgOffField.getText());
        System.out.println("Макс. вкл.: " + maxOnField.getText());
        System.out.println("Макс. выкл.: " + maxOffField.getText());
        System.out.println("Дребезг НЗ: " + debounceNCField.getText());
        System.out.println("Дребезг НО: " + debounceNOField.getText());

        System.out.println("Макс прямой: " + maxDirectField.getText());
        System.out.println("Мин прямой: " + minDirectField.getText());
        System.out.println("Макс допол.: " + maxAdditionalField.getText());
        System.out.println("Мин допол.: " + minAdditionalField.getText());
        System.out.println("Макс дифф.: " + maxDiffField.getText());
        System.out.println("Мин дифф.: " + minDiffField.getText());

        System.out.println("Макс НЗ: " + maxNCField.getText());
        System.out.println("Мин НО: " + minNOField.getText());

        // Сбор битмаски и запись в Modbus регистр
        int bitmask = buildBitmaskFromFields();
        System.out.println("Записываем битмаску: " + Integer.toBinaryString(bitmask));

        try {
            ModbusService.getInstance().writeSingleRegister(1, 0, bitmask);
            System.out.println("Запись в Modbus прошла успешно");
        } catch (Exception e) {
            e.printStackTrace();
            // Можно добавить всплывающее окно об ошибке, если нужно
        }

        // Переход на главное меню
        handleSaveParameters();
    }

    @FXML
    private void handleSaveParameters() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo6/mainMenuScreen.fxml"));
            Parent mainMenu = loader.load();
            saveParametersButton.getScene().setRoot(mainMenu);
        } catch (IOException e) {
            e.printStackTrace();
            // Можно показать Alert об ошибке загрузки
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo6/mainMenuScreen.fxml"));
            Parent mainMenu = loader.load();
            backButton.getScene().setRoot(mainMenu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
