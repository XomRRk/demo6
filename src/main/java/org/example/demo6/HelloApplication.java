package org.example.demo6;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Подключаемся к Modbus один раз при старте приложения
        ModbusService.getInstance().connect();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/demo6/mainMenuScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 845, 475);
        /*stage.setTitle("Hello!");*/
        stage.setScene(scene);
        stage.show();
    }
    @Override
    public void stop() throws Exception {
        // Отключаемся при закрытии приложения
        ModbusService.getInstance().disconnect();
        super.stop();
    }


    public static void main(String[] args) {
        launch();
    }
}