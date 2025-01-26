package enrollium.client.page.students;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

public class TradeSection extends BasePage {
    public static final TranslationKey NAME = TranslationKey.TradeSection;

    // Table components
    private final TableView<Section> userDetailsTable = new TableView<>();
    private final ObservableList<Section> sectionDataList = FXCollections.observableArrayList();

    // Table columns
    private final TableColumn<Section, String> emailColumn = new TableColumn<>("Email ID");
    private final TableColumn<Section, String> sectionNameColumn = new TableColumn<>("Section Name");
    private final TableColumn<Section, String> noteColumn = new TableColumn<>("Note");

    public TradeSection() {
        super();

        addPageHeader();

        // Create and configure the Swap and Offered Trade sections
        VBox swapSection = createSwapSection();
        VBox tradeSection = createTradeSection();

        // Create and configure the user details table
        VBox userDetailsTableView = createUserDetailsTable();

        // Add sections to the page
        addSection("Swap Course", swapSection);
        addSection("Offered Trade", tradeSection);
        addSection("User Details Table", userDetailsTableView);

        // Load routine data
        loadRoutineData();
    }

    private VBox createSwapSection() {
        Label selectCourseLabel = new Label("Select your Course:");
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll("Course 1", "Course 2", "Course 3");

        Label partnerCourseIdLabel = new Label("Partner's Course ID:");
        TextField partnerCourseIdField = new TextField();

        VBox swapSection = new VBox(10, selectCourseLabel, courseComboBox,
                partnerCourseIdLabel, partnerCourseIdField);
        swapSection.setPadding(new Insets(20));
        return swapSection;
    }

    private VBox createTradeSection() {
        Label selectCourseLabel = new Label("Select your Course:");
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll("Course 1", "Course 2", "Course 3");

        Label noteLabel = new Label("Note:");
        TextArea noteField = new TextArea();
        noteField.setPromptText("Enter your note");
        noteField.setWrapText(true);
        noteField.setMaxSize(300, 120);
        noteField.setMinSize(300, 120);

        Button sendButton = new Button("Send");

        VBox tradeSection = new VBox(10, selectCourseLabel, courseComboBox,
                noteLabel, noteField, sendButton);
        tradeSection.setPadding(new Insets(20));
        return tradeSection;
    }

    private VBox createUserDetailsTable() {
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        sectionNameColumn.setCellValueFactory(data -> data.getValue().sectionNameProperty());
        noteColumn.setCellValueFactory(data -> data.getValue().noteProperty());

        userDetailsTable.getColumns().addAll(emailColumn, sectionNameColumn, noteColumn);

        userDetailsTable.setItems(sectionDataList);

        VBox container = new VBox(10, userDetailsTable);
        container.setPadding(new Insets(20));
        VBox.setVgrow(userDetailsTable, Priority.ALWAYS);

        return container;
    }

    private void loadRoutineData() {
        sectionDataList.add(new Section("john.doe@example.com", "Section A", "Trade approved"));
        sectionDataList.add(new Section("jane.smith@example.com", "Section B", "Pending approval"));
        sectionDataList.add(new Section("alex.jones@example.com", "Section C", "Rejected"));
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}

class Section {
    private final StringProperty email;
    private final StringProperty sectionName;
    private final StringProperty note;

    public Section(String email, String sectionName, String note) {
        this.email = new SimpleStringProperty(email);
        this.sectionName = new SimpleStringProperty(sectionName);
        this.note = new SimpleStringProperty(note);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty sectionNameProperty() {
        return sectionName;
    }

    public StringProperty noteProperty() {
        return note;
    }
}
