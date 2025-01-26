package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Data;
import net.datafaker.Faker;

import java.time.DayOfWeek;
import java.util.UUID;

public class SpaceTimePage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SPACE_TIME;

    // Table components
    private final TableView<SpaceTimeData> tableView = new TableView<>();
    private final ObservableList<SpaceTimeData> spaceTimeDataList = FXCollections.observableArrayList();
    private final Faker faker = new Faker();

    // Form components
    private Label selectedIdLabel;
    private TextField roomNameField;
    private TextField roomNumberField;
    private ComboBox<String> dayOfWeekDropdown;
    private TextField timeSlotField;

    public SpaceTimePage() {
        super();
        addPageHeader();
        addFormattedText("Manage classroom scheduling with room, day, and timeslot.");
        addSection("Space-Time Table", createSpaceTimeTable());
        addSection("Space-Time Form", createSpaceTimeForm());
        loadMockData();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createSpaceTimeTable() {
        // Define table columns
        TableColumn<SpaceTimeData, String> idColumn = new TableColumn<>("ID");
        TableColumn<SpaceTimeData, String> versionColumn = new TableColumn<>("Version");
        TableColumn<SpaceTimeData, String> roomNameColumn = new TableColumn<>("Room Name");
        TableColumn<SpaceTimeData, String> roomNumberColumn = new TableColumn<>("Room Number");
        TableColumn<SpaceTimeData, String> dayOfWeekColumn = new TableColumn<>("Day of Week");
        TableColumn<SpaceTimeData, String> timeSlotColumn = new TableColumn<>("Time Slot");

        // Bind columns to SpaceTimeData properties
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        versionColumn.setCellValueFactory(data -> new SimpleStringProperty("1.0")); // Placeholder version value
        roomNameColumn.setCellValueFactory(data -> data.getValue().roomNameProperty());
        roomNumberColumn.setCellValueFactory(data -> data.getValue().roomNumberProperty());
        dayOfWeekColumn.setCellValueFactory(data -> data.getValue().dayOfWeekProperty());
        timeSlotColumn.setCellValueFactory(data -> data.getValue().timeSlotProperty());

        // Add copy to clipboard for ID column
        idColumn.setCellFactory(tc -> {
            TableCell<SpaceTimeData, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem());
                    Clipboard.getSystemClipboard().setContent(content);
                    showNotification("Copied ID: " + cell.getItem(), "INFO");
                }
            });
            return cell;
        });

        // Configure table to match CoursePage
        tableView.setItems(spaceTimeDataList);
        tableView.getColumns().clear();
        tableView.getColumns().addAll(idColumn, versionColumn, roomNameColumn, roomNumberColumn, dayOfWeekColumn, timeSlotColumn);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DatabaseUiUtils.styleCourseTableView(tableView);

        // Row selection handler
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedIdLabel.setText("Selected ID: " + selected.getId());
                roomNameField.setText(selected.getRoomName());
                roomNumberField.setText(selected.getRoomNumber());
                dayOfWeekDropdown.setValue(selected.getDayOfWeek());
                timeSlotField.setText(selected.getTimeSlot());
            }
        });

        VBox container = new VBox(10, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return container;
    }

    private VBox createSpaceTimeForm() {
        // Initialize form components
        selectedIdLabel = new Label("No selection");
        Button clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setOnAction(e -> clearForm());

        HBox selectionControls = new HBox(10, selectedIdLabel, clearSelectionBtn);
        selectionControls.setAlignment(Pos.CENTER_LEFT);

        roomNameField = new TextField();
        roomNumberField = new TextField();
        dayOfWeekDropdown = new ComboBox<>(FXCollections.observableArrayList(
                DayOfWeek.MONDAY.name(), DayOfWeek.TUESDAY.name(), DayOfWeek.WEDNESDAY.name(),
                DayOfWeek.THURSDAY.name(), DayOfWeek.FRIDAY.name(), DayOfWeek.SATURDAY.name(), DayOfWeek.SUNDAY.name()
        ));
        timeSlotField = new TextField();

        VBox form = new VBox(10,
                selectionControls,
                new Label("Room Name:"), roomNameField,
                new Label("Room Number:"), roomNumberField,
                new Label("Day of Week:"), dayOfWeekDropdown,
                new Label("Time Slot (1-6):"), timeSlotField
        );

        VBox actions = DatabaseUiUtils.createActionButtons(
                this::createSpaceTime,
                this::updateSpaceTime,
                this::deleteSpaceTime,
                this::loadMockData
        );

        VBox container = new VBox(20, form, actions);
        container.setPadding(new Insets(20));
        return container;
    }

    private void clearForm() {
        tableView.getSelectionModel().clearSelection();
        selectedIdLabel.setText("No selection");
        roomNameField.clear();
        roomNumberField.clear();
        dayOfWeekDropdown.setValue(null);
        timeSlotField.clear();
    }

    private void createSpaceTime() {
        spaceTimeDataList.add(new SpaceTimeData(
                UUID.randomUUID().toString(),
                roomNameField.getText(),
                roomNumberField.getText(),
                dayOfWeekDropdown.getValue(),
                timeSlotField.getText()
        ));
        clearForm();
    }

    private void updateSpaceTime() {
        SpaceTimeData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setRoomName(roomNameField.getText());
            selected.setRoomNumber(roomNumberField.getText());
            selected.setDayOfWeek(dayOfWeekDropdown.getValue());
            selected.setTimeSlot(timeSlotField.getText());
            tableView.refresh();
            clearForm();
        }
    }

    private void deleteSpaceTime() {
        SpaceTimeData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            spaceTimeDataList.remove(selected);
            clearForm();
        }
    }

    private void loadMockData() {
        spaceTimeDataList.clear();
        for (int i = 0; i < 10; i++) {
            spaceTimeDataList.add(new SpaceTimeData(
                    UUID.randomUUID().toString(),
                    faker.university().name(),
                    faker.address().buildingNumber(),
                    DayOfWeek.of(faker.number().numberBetween(1, 7)).name(),
                    String.valueOf(faker.number().numberBetween(1, 6))
            ));
        }
    }

    private void showNotification(String message, String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Default to INFO type
        if ("WARNING".equals(type)) {
            alert.setAlertType(Alert.AlertType.WARNING);
        } else if ("DANGER".equals(type)) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Data
    public static class SpaceTimeData {
        private final StringProperty id;
        private final StringProperty roomName;
        private final StringProperty roomNumber;
        private final StringProperty dayOfWeek;
        private final StringProperty timeSlot;

        public SpaceTimeData(String id, String roomName, String roomNumber, String dayOfWeek, String timeSlot) {
            this.id = new SimpleStringProperty(id);
            this.roomName = new SimpleStringProperty(roomName);
            this.roomNumber = new SimpleStringProperty(roomNumber);
            this.dayOfWeek = new SimpleStringProperty(dayOfWeek);
            this.timeSlot = new SimpleStringProperty(timeSlot);
        }

        public StringProperty idProperty() {
            return id;
        }

        public StringProperty roomNameProperty() {
            return roomName;
        }

        public StringProperty roomNumberProperty() {
            return roomNumber;
        }

        public StringProperty dayOfWeekProperty() {
            return dayOfWeek;
        }

        public StringProperty timeSlotProperty() {
            return timeSlot;
        }

        public void setRoomName(String roomName) {
            this.roomName.set(roomName);
        }

        public void setRoomNumber(String roomNumber) {
            this.roomNumber.set(roomNumber);
        }

        public void setDayOfWeek(String dayOfWeek) {
            this.dayOfWeek.set(dayOfWeek);
        }

        public void setTimeSlot(String timeSlot) {
            this.timeSlot.set(timeSlot);
        }

        public String getId() {
            return id.get();
        }

        public String getRoomName() {
            return roomName.get();
        }

        public String getRoomNumber() {
            return roomNumber.get();
        }

        public String getDayOfWeek() {
            return dayOfWeek.get();
        }

        public String getTimeSlot() {
            return timeSlot.get();
        }
    }
}
