package org.example.demo6;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.util.Locale;

public class HelloController {

    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private TableView<Data> dataTable;
    @FXML
    private TableColumn<Data, String> timeColumn;
    @FXML
    private TableColumn<Data, String> debrisColumn;
    @FXML
    private TableColumn<Data, String> positionColumn;
    @FXML
    private TableColumn<Data, String> speedColumn;

    @FXML
    private Label positionLabel;
    @FXML
    private Label speedLabel;
    @FXML
    private Label forceLabel;

    @FXML
    private AreaChart<String, Number> chart;

    @FXML
    private ImageView positionArrow;
    @FXML
    private ImageView speedArrow;
    @FXML
    private ImageView effortArrow;

    @FXML
    private CheckBox positionCheckBox;
    @FXML
    private CheckBox speedCheckBox;
    @FXML
    private CheckBox debrisCheckBox;

    private final XYChart.Series<String, Number> positionSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> speedSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> debrisSeries = new XYChart.Series<>();

    private final ObservableList<Data> dataList = FXCollections.observableArrayList();
    private volatile boolean collectingData = false;
    private Task<Void> dataCollectionTask;
    private int counter = 0;

    @FXML
    private void handleStartButtonAction() {
        collectingData = true;
        startDataCollection();
    }

    @FXML
    private void handleStopButtonAction() {
        collectingData = false;
        if (dataCollectionTask != null && dataCollectionTask.isRunning()) {
            dataCollectionTask.cancel(true);
        }
        loadNewScreen();  // Загружаем новый экран после остановки
    }

    private void startDataCollection() {
        dataCollectionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (collectingData) {
                    if (isCancelled()) break;

                    Thread.sleep(1000);
                    String time = String.format(Locale.US, "%.1f", 3000 + Math.random() * 2000);
                    String debris = String.format(Locale.US, "%.1f", Math.random() * 5);
                    String position = String.format(Locale.US, "%.1f", Math.random() * 20);
                    String speed = String.format(Locale.US, "%.1f", Math.random() * 10);

                    Data newData = new Data(time, debris, position, speed);

                    if (collectingData) {
                        dataList.add(newData);
                        updateTable();
                    }

                    updateGraphs(position, speed, debris);
                    counter++;
                }
                return null;
            }
        };
        new Thread(dataCollectionTask).start();
    }

    private void updateTable() {
        Platform.runLater(() -> dataTable.setItems(dataList));
    }

    private void updateGraphs(String position, String speed, String debris) {
        Platform.runLater(() -> {
            positionLabel.setText(position);
            speedLabel.setText(speed);
            forceLabel.setText(debris);

            String timeLabel = String.valueOf(counter);

            try {
                double positionVal = Double.parseDouble(position);
                double speedVal = Double.parseDouble(speed);
                double effortVal = Double.parseDouble(debris);

                positionSeries.getData().add(new XYChart.Data<>(timeLabel, positionVal));
                speedSeries.getData().add(new XYChart.Data<>(timeLabel, speedVal));
                debrisSeries.getData().add(new XYChart.Data<>(timeLabel, effortVal));

                if (positionSeries.getData().size() > 30) positionSeries.getData().remove(0);
                if (speedSeries.getData().size() > 30) speedSeries.getData().remove(0);
                if (debrisSeries.getData().size() > 30) debrisSeries.getData().remove(0);

                rotateArrow(positionArrow, positionVal, 0, 20);
                rotateArrow(speedArrow, speedVal, 0, 10);
                rotateArrow(effortArrow, effortVal, 0, 5);

            } catch (NumberFormatException ignored) {
            }
        });
    }

    private void rotateArrow(ImageView arrow, double value, double min, double max) {
        double minAngle = -90;
        double maxAngle = 90;
        double angle = minAngle + (value - min) / (max - min) * (maxAngle - minAngle);
        arrow.setRotate(angle);
    }

    private void loadNewScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo6/newScreen.fxml"));
            Parent newScreen = loader.load();
            AnchorPane rootContainer = (AnchorPane) startButton.getScene().getRoot();
            rootContainer.getChildren().setAll(newScreen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Data {
        private final StringProperty time;
        private final StringProperty debris;
        private final StringProperty position;
        private final StringProperty speed;

        public Data(String time, String debris, String position, String speed) {
            this.time = new SimpleStringProperty(time);
            this.debris = new SimpleStringProperty(debris);
            this.position = new SimpleStringProperty(position);
            this.speed = new SimpleStringProperty(speed);
        }

        public String getTime() { return time.get(); }
        public String getDebris() { return debris.get(); }
        public String getPosition() { return position.get(); }
        public String getSpeed() { return speed.get(); }

        public StringProperty timeProperty() { return time; }
        public StringProperty debrisProperty() { return debris; }
        public StringProperty positionProperty() { return position; }
        public StringProperty speedProperty() { return speed; }
    }

    @FXML
    private void initialize() {
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        debrisColumn.setCellValueFactory(cellData -> cellData.getValue().debrisProperty());
        positionColumn.setCellValueFactory(cellData -> cellData.getValue().positionProperty());
        speedColumn.setCellValueFactory(cellData -> cellData.getValue().speedProperty());

        // Отображаем только одну серию по умолчанию
        chart.getData().add(positionSeries);
        positionSeries.setName("Положение");
        speedSeries.setName("Скорость");
        debrisSeries.setName("Дребезг");
        chart.setLegendVisible(false);
        positionCheckBox.setSelected(true);

        // Обработчики чекбоксов
        positionCheckBox.setOnAction(e -> showOnlySeries(positionSeries));
        speedCheckBox.setOnAction(e -> showOnlySeries(speedSeries));
        debrisCheckBox.setOnAction(e -> showOnlySeries(debrisSeries));

        // Настройки оси Y
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(25);
        yAxis.setTickUnit(5);
    }

    private void showOnlySeries(XYChart.Series<String, Number> seriesToShow) {
        chart.getData().clear();
        chart.getData().add(seriesToShow);

        positionCheckBox.setSelected(seriesToShow == positionSeries);
        speedCheckBox.setSelected(seriesToShow == speedSeries);
        debrisCheckBox.setSelected(seriesToShow == debrisSeries);
    }
}
