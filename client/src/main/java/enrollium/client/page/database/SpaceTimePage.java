package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.datafaker.Faker;

import java.time.DayOfWeek;
import java.util.UUID;

public class SpaceTimePage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SPACE_TIME;
    private final TableView<SpaceTimeData> tableView = new TableView<>();
    private final ObservableList<SpaceTimeData> spaceTimeDataList = FXCollections.observableArrayList();
    private final Faker faker = new Faker();
    private final TableColumn<SpaceTimeData, String> idColumn = new TableColumn<>("ID");
    private final TableColumn<SpaceTimeData, String> roomNameColumn = new TableColumn<>("Room Name");
    private final TableColumn<SpaceTimeData, String> roomNumberColumn = new TableColumn<>("Room Number");
    private final TableColumn<SpaceTimeData, String> dayOfWeekColumn = new TableColumn<>("Day of Week");
    private final TableColumn<SpaceTimeData, String> timeSlotColumn = new TableColumn<>("Time Slot");
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
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createSpaceTimeTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        roomNameColumn.setCellValueFactory(data -> data.getValue().roomNameProperty());
        roomNumberColumn.setCellValueFactory(data -> data.getValue().roomNumberProperty());
        dayOfWeekColumn.setCellValueFactory(data -> data.getValue().dayOfWeekProperty());
        timeSlotColumn.setCellValueFactory(data -> data.getValue().timeSlotProperty());

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
                }
            });
            return cell;
        });

        tableView.getColumns().addAll(idColumn, roomNameColumn, roomNumberColumn, dayOfWeekColumn, timeSlotColumn);
        tableView.setItems(spaceTimeDataList);
        tableView.setRowFactory(tv -> {
            TableRow<SpaceTimeData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    SpaceTimeData rowData = row.getItem();
                    roomNameField.setText(rowData.getRoomName());
                    roomNumberField.setText(rowData.getRoomNumber());
                    dayOfWeekDropdown.setValue(rowData.getDayOfWeek());
                    timeSlotField.setText(rowData.getTimeSlot());
                }
            });
            return row;
        });

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createSpaceTimeForm() {
        roomNameField = new TextField();
        roomNumberField = new TextField();
        dayOfWeekDropdown = new ComboBox<>(FXCollections.observableArrayList(
                DayOfWeek.MONDAY.name(), DayOfWeek.TUESDAY.name(), DayOfWeek.WEDNESDAY.name(),
                DayOfWeek.THURSDAY.name(), DayOfWeek.FRIDAY.name(), DayOfWeek.SATURDAY.name(), DayOfWeek.SUNDAY.name()
        ));
        timeSlotField = new TextField();

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button fillDemoBtn = new Button("Fill for Demo");

        createBtn.setOnAction(e -> {
            spaceTimeDataList.add(new SpaceTimeData(
                    UUID.randomUUID().toString(),
                    roomNameField.getText(),
                    roomNumberField.getText(),
                    dayOfWeekDropdown.getValue(),
                    timeSlotField.getText()
            ));
        });

        updateBtn.setOnAction(e -> {
            SpaceTimeData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setRoomName(roomNameField.getText());
                selected.setRoomNumber(roomNumberField.getText());
                selected.setDayOfWeek(dayOfWeekDropdown.getValue());
                selected.setTimeSlot(timeSlotField.getText());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            SpaceTimeData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                spaceTimeDataList.remove(selected);
            }
        });

        fillDemoBtn.setOnAction(e -> loadMockData());

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn, fillDemoBtn);
        actions.setAlignment(Pos.CENTER);

        VBox form = new VBox(10,
                new Label("Room Name:"), roomNameField,
                new Label("Room Number:"), roomNumberField,
                new Label("Day of Week:"), dayOfWeekDropdown,
                new Label("Time Slot (1-6):"), timeSlotField,
                actions
        );

        form.setPadding(new Insets(10));
        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            spaceTimeDataList.add(new SpaceTimeData(UUID.randomUUID().toString(), faker.university()
                                                                                       .name(), faker.address()
                                                                                                     .buildingNumber(),
                    DayOfWeek.of(faker.number().numberBetween(1, 7)).name(), String.valueOf(faker.number()
                                                                                                 .numberBetween(1, 6))));
        }
    }
}
