package enrollium.client.page.general;

import enrollium.client.page.OutlinePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;


public class SpaceTimeTableView extends OutlinePage {
    public static final TranslationKey            NAME = TranslationKey.SPACETIME;
    private final       ObservableList<SpaceTime> spaceTimeData;

    // Default constructor with some initial content
    public SpaceTimeTableView() {
        this(FXCollections.observableArrayList(
                new SpaceTime("Room 101", "101", "Theory", "Monday", "08:00 - 09:30"),
                new SpaceTime("Room 102", "102", "Lab", "Tuesday", "10:00 - 11:30"),
                new SpaceTime("Room 201", "201", "Seminar", "Wednesday", "13:00 - 14:30"),
                new SpaceTime("Room 301", "301", "Conference", "Thursday", "15:00 - 16:30"),
                new SpaceTime("Room 401", "401", "Theory", "Friday", "17:00 - 18:30")
        ));
    }

    // Constructor with ObservableList parameter
    public SpaceTimeTableView(ObservableList<SpaceTime> spaceTimeData) {
        super();
        this.spaceTimeData = spaceTimeData;

        // Add page header
        addPageHeader();

        // Add a section for the table
        addSection("SpaceTime Table", createTableView());
    }

    @Override
    protected void updateTexts() {

    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createTableView() {
        VBox root = new VBox(10);

        TableView<SpaceTime> table = new TableView<>();

        // Define Columns
        TableColumn<SpaceTime, String> roomNameColumn = new TableColumn<>("Room Name");
        roomNameColumn.setCellValueFactory(data -> data.getValue().roomNameProperty());

        TableColumn<SpaceTime, String> roomNumberColumn = new TableColumn<>("Room Number");
        roomNumberColumn.setCellValueFactory(data -> data.getValue().roomNumberProperty());

        TableColumn<SpaceTime, String> roomTypeColumn = new TableColumn<>("Room Type");
        roomTypeColumn.setCellValueFactory(data -> data.getValue().roomTypeProperty());

        TableColumn<SpaceTime, String> dayColumn = new TableColumn<>("Day");
        dayColumn.setCellValueFactory(data -> data.getValue().dayProperty());

        TableColumn<SpaceTime, String> timeSlotColumn = new TableColumn<>("Time Slot");
        timeSlotColumn.setCellValueFactory(data -> data.getValue().timeSlotProperty());

        // Add columns to table
        table.getColumns().addAll(roomNameColumn, roomNumberColumn, roomTypeColumn, dayColumn, timeSlotColumn);

        // Bind TableView to shared ObservableList
        table.setItems(spaceTimeData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        root.getChildren().addAll(new Label("SpaceTime Slots Overview"), table);
        return root;
    }

    // Mock SpaceTime class
    public static class SpaceTime {
        private final SimpleStringProperty roomName;
        private final SimpleStringProperty roomNumber;
        private final SimpleStringProperty roomType;
        private final SimpleStringProperty day;
        private final SimpleStringProperty timeSlot;

        public SpaceTime(String roomName, String roomNumber, String roomType, String day, String timeSlot) {
            this.roomName   = new SimpleStringProperty(roomName);
            this.roomNumber = new SimpleStringProperty(roomNumber);
            this.roomType   = new SimpleStringProperty(roomType);
            this.day        = new SimpleStringProperty(day);
            this.timeSlot   = new SimpleStringProperty(timeSlot);
        }

        public String getRoomName() {
            return roomName.get();
        }

        public SimpleStringProperty roomNameProperty() {
            return roomName;
        }

        public String getRoomNumber() {
            return roomNumber.get();
        }

        public SimpleStringProperty roomNumberProperty() {
            return roomNumber;
        }

        public String getRoomType() {
            return roomType.get();
        }

        public SimpleStringProperty roomTypeProperty() {
            return roomType;
        }

        public String getDay() {
            return day.get();
        }

        public SimpleStringProperty dayProperty() {
            return day;
        }

        public String getTimeSlot() {
            return timeSlot.get();
        }

        public SimpleStringProperty timeSlotProperty() {
            return timeSlot;
        }
    }
}
