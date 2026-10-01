package fi.metropolia.tempconverter;

import fi.metropolia.tempconverter.dao.TempRecordDAO;
import fi.metropolia.tempconverter.model.TempCalculator;
import fi.metropolia.tempconverter.model.TempRecord;
import fi.metropolia.tempconverter.model.TemperatureUnit;
import fi.metropolia.tempconverter.dao.TemperatureUnitDAO;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class MainApp extends Application {

    private final TempCalculator calculator = new TempCalculator();
    private final TempRecordDAO recordDAO = new TempRecordDAO();
    private final TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();

    private TextField inputField;
    private ComboBox<String> conversionBox;
    private Label resultLabel;
    private Label statusLabel;
    private TableView<TempRecord> table;

    private final ObservableList<TempRecord> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Temperature Converter - Metropolia");

        Label title = new Label("Temperature Converter");
        title.setFont(new Font("Arial", 22));

        inputField = new TextField();
        inputField.setPromptText("Enter a value");

        conversionBox = new ComboBox<>();
        conversionBox.getItems().addAll(
                "Fahrenheit -> Celsius",
                "Celsius -> Fahrenheit",
                "Kelvin -> Celsius",
                "Celsius -> Kelvin");
        conversionBox.getSelectionModel().selectFirst();

        Button convertBtn = new Button("Convert & Save");
        convertBtn.setOnAction(e -> convertAndSave());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadRecords());

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setOnAction(e -> deleteSelected());

        resultLabel = new Label("Result: -");
        resultLabel.setFont(new Font("Arial", 16));

        statusLabel = new Label("Ready");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.add(new Label("Value:"), 0, 0);
        form.add(inputField, 1, 0);
        form.add(new Label("Conversion:"), 0, 1);
        form.add(conversionBox, 1, 1);

        HBox buttons = new HBox(10, convertBtn, refreshBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        buildTable();

        VBox root = new VBox(12,
                title,
                form,
                buttons,
                resultLabel,
                new Label("Saved records:"),
                table,
                statusLabel);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 650, 520));
        stage.show();

        loadUnits();
        loadRecords();
    }

    private void buildTable() {
        table = new TableView<>();

        TableColumn<TempRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("recordId"));

        TableColumn<TempRecord, Double> inCol = new TableColumn<>("Input");
        inCol.setCellValueFactory(new PropertyValueFactory<>("inputValue"));

        TableColumn<TempRecord, Double> outCol = new TableColumn<>("Converted");
        outCol.setCellValueFactory(new PropertyValueFactory<>("convertedValue"));

        TableColumn<TempRecord, Integer> unitCol = new TableColumn<>("Unit ID");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));

        TableColumn<TempRecord, Object> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setPrefWidth(160);

        table.getColumns().add(idCol);
        table.getColumns().add(inCol);
        table.getColumns().add(outCol);
        table.getColumns().add(unitCol);
        table.getColumns().add(dateCol);

        table.setItems(data);
        table.setPrefHeight(220);
    }

    private void convertAndSave() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) {
            showStatus("Please enter a value", true);
            return;
        }

        double value;
        try {
            value = Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
            showStatus("Invalid number", true);
            return;
        }

        String choice = conversionBox.getValue();
        double result;
        int unitId;

        if ("Fahrenheit -> Celsius".equals(choice)) {
            result = calculator.fahrenheitToCelsius(value);
            unitId = 1;
        } else if ("Celsius -> Fahrenheit".equals(choice)) {
            result = calculator.celsiusToFahrenheit(value);
            unitId = 2;
        } else if ("Kelvin -> Celsius".equals(choice)) {
            result = calculator.kelvinToCelsius(value);
            unitId = 1;
        } else {
            result = calculator.celsiusToKelvin(value);
            unitId = 3;
        }

        String extra = calculator.isExtremeTemperature(result) ? "  (EXTREME!)" : "";
        resultLabel.setText(String.format("Result: %.2f%s", result, extra));

        try {
            recordDAO.insert(new TempRecord(value, result, unitId));
            showStatus("Saved to database", false);
            loadRecords();
        } catch (SQLException ex) {
            showStatus("DB error: " + ex.getMessage(), true);
        }
    }

    private void loadRecords() {
        try {
            List<TempRecord> records = recordDAO.findAll();
            data.setAll(records);
            showStatus("Loaded " + records.size() + " record(s)", false);
        } catch (SQLException ex) {
            showStatus("Cannot load records: " + ex.getMessage(), true);
        }
    }

    private void loadUnits() {
        try {
            List<TemperatureUnit> units = unitDAO.findAll();
            showStatus("Connected. Units in DB: " + units.size(), false);
        } catch (SQLException ex) {
            showStatus("No DB connection: " + ex.getMessage(), true);
        }
    }

    private void deleteSelected() {
        TempRecord selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a row first", true);
            return;
        }
        try {
            recordDAO.delete(selected.getRecordId());
            showStatus("Deleted record " + selected.getRecordId(), false);
            loadRecords();
        } catch (SQLException ex) {
            showStatus("Delete failed: " + ex.getMessage(), true);
        }
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}