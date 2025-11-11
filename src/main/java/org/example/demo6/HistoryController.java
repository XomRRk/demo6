package org.example.demo6;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HistoryController {

    @FXML private TableView<TestResult> testResultsTable;
    @FXML private TableColumn<TestResult, String> serialNumberColumn;
    @FXML private TableColumn<TestResult, String> switchTypeColumn;
    @FXML private TableColumn<TestResult, Timestamp> testDateColumn;
    @FXML private TableColumn<TestResult, Double> directTravelColumn;
    @FXML private TableColumn<TestResult, Double> additionalTravelColumn;
    @FXML private TableColumn<TestResult, Double> differentialTravelColumn;
    @FXML private TableColumn<TestResult, Double> actuationForceDirectColumn;
    @FXML private TableColumn<TestResult, Double> actuationForceReverseColumn;
    @FXML private TableColumn<TestResult, Boolean> noStickingColumn;
    @FXML private TableColumn<TestResult, Double> movementSpeedColumn;
    @FXML private TableColumn<TestResult, Double> voltageDropNoColumn;
    @FXML private TableColumn<TestResult, Double> voltageDropNcColumn;
    @FXML private TableColumn<TestResult, String> statusColumn;

    @FXML private TextField serialNumberFilter;
    @FXML private TextField statusFilter;
    @FXML private TextField switchTypeFilter;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button backButton;

    private ObservableList<TestResult> testResultsData;

    @FXML
    private void initialize() {
        serialNumberColumn.setCellValueFactory(cellData -> cellData.getValue().serialNumberProperty());
        switchTypeColumn.setCellValueFactory(cellData -> cellData.getValue().switchTypeProperty());
        testDateColumn.setCellValueFactory(cellData -> cellData.getValue().testDateProperty());
        directTravelColumn.setCellValueFactory(cellData -> cellData.getValue().directTravelProperty().asObject());
        additionalTravelColumn.setCellValueFactory(cellData -> cellData.getValue().additionalTravelProperty().asObject());
        differentialTravelColumn.setCellValueFactory(cellData -> cellData.getValue().differentialTravelProperty().asObject());
        actuationForceDirectColumn.setCellValueFactory(cellData -> cellData.getValue().actuationForceDirectProperty().asObject());
        actuationForceReverseColumn.setCellValueFactory(cellData -> cellData.getValue().actuationForceReverseProperty().asObject());
        noStickingColumn.setCellValueFactory(cellData -> cellData.getValue().noStickingProperty());
        movementSpeedColumn.setCellValueFactory(cellData -> cellData.getValue().movementSpeedProperty().asObject());
        voltageDropNoColumn.setCellValueFactory(cellData -> cellData.getValue().voltageDropNoProperty().asObject());
        voltageDropNcColumn.setCellValueFactory(cellData -> cellData.getValue().voltageDropNcProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        testResultsData = FXCollections.observableArrayList();
        loadTestResults();
    }

    private void loadTestResults() {
        String query = "SELECT t.serial_number, t.test_date, t.direct_travel, t.additional_travel, t.differential_travel, " +
                "t.actuation_force_direct, t.actuation_force_reverse, t.no_sticking, t.movement_speed, t.voltage_drop_no, " +
                "t.voltage_drop_nc, t.status, m.type AS switch_type " +
                "FROM public.test_results t " +
                "JOIN public.micro_switches m ON t.switch_id = m.id " +
                "ORDER BY t.test_date DESC";

        try (Connection connection = DatabaseConnector.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                testResultsData.add(new TestResult(
                        rs.getString("serial_number"),
                        rs.getTimestamp("test_date"),
                        rs.getDouble("direct_travel"),
                        rs.getDouble("additional_travel"),
                        rs.getDouble("differential_travel"),
                        rs.getDouble("actuation_force_direct"),
                        rs.getDouble("actuation_force_reverse"),
                        rs.getBoolean("no_sticking"),
                        rs.getDouble("movement_speed"),
                        rs.getDouble("voltage_drop_no"),
                        rs.getDouble("voltage_drop_nc"),
                        rs.getString("status"),
                        rs.getString("switch_type")
                ));
            }

            testResultsTable.setItems(testResultsData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String serialNumber = serialNumberFilter.getText();
        String status = statusFilter.getText();
        String switchType = switchTypeFilter.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT t.serial_number, t.test_date, t.direct_travel, t.additional_travel, t.differential_travel, " +
                        "t.actuation_force_direct, t.actuation_force_reverse, t.no_sticking, t.movement_speed, " +
                        "t.voltage_drop_no, t.voltage_drop_nc, t.status, m.type AS switch_type " +
                        "FROM public.test_results t JOIN public.micro_switches m ON t.switch_id = m.id WHERE 1=1"
        );

        if (!serialNumber.isEmpty()) queryBuilder.append(" AND t.serial_number ILIKE ?");
        if (!status.isEmpty()) queryBuilder.append(" AND t.status ILIKE ?");
        if (!switchType.isEmpty()) queryBuilder.append(" AND m.type ILIKE ?");
        if (startDate != null && endDate != null) queryBuilder.append(" AND t.test_date::date BETWEEN ? AND ?");
        else if (startDate != null) queryBuilder.append(" AND t.test_date::date >= ?");
        else if (endDate != null) queryBuilder.append(" AND t.test_date::date <= ?");

        queryBuilder.append(" ORDER BY t.test_date DESC");

        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;
            if (!serialNumber.isEmpty()) stmt.setString(paramIndex++, "%" + serialNumber + "%");
            if (!status.isEmpty()) stmt.setString(paramIndex++, "%" + status + "%");
            if (!switchType.isEmpty()) stmt.setString(paramIndex++, "%" + switchType + "%");
            if (startDate != null && endDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(startDate));
                stmt.setDate(paramIndex++, Date.valueOf(endDate));
            } else if (startDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(startDate));
            } else if (endDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(endDate));
            }

            ResultSet rs = stmt.executeQuery();
            testResultsData.clear();

            while (rs.next()) {
                testResultsData.add(new TestResult(
                        rs.getString("serial_number"),
                        rs.getTimestamp("test_date"),
                        rs.getDouble("direct_travel"),
                        rs.getDouble("additional_travel"),
                        rs.getDouble("differential_travel"),
                        rs.getDouble("actuation_force_direct"),
                        rs.getDouble("actuation_force_reverse"),
                        rs.getBoolean("no_sticking"),
                        rs.getDouble("movement_speed"),
                        rs.getDouble("voltage_drop_no"),
                        rs.getDouble("voltage_drop_nc"),
                        rs.getString("status"),
                        rs.getString("switch_type")
                ));
            }

            testResultsTable.setItems(testResultsData);

        } catch (SQLException e) {
            e.printStackTrace();
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
