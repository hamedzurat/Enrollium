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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Data;
import net.datafaker.Faker;

import java.util.UUID;

public class SectionPage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SECTION;

    // Table components
    private final TableView<SectionData> tableView = new TableView<>();
    private final ObservableList<SectionData> sectionDataList = FXCollections.observableArrayList();
    private final Faker faker = new Faker();

    // Form components
    private Label selectedIdLabel;
    private TextField nameField;
    private ComboBox<String> subjectDropdown;
    private ComboBox<String> trimesterDropdown;
    private TextField maxCapacityField;

    public SectionPage() {
        super();
        addPageHeader();
        addFormattedText("Manage course sections with their subject, trimester, and capacities.");
        addSection("Sections Table", createSectionTable());
        addSection("Section Form", createSectionForm());
        loadMockData(); // Populate with sample data
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createSectionTable() {
        // Define table columns
        TableColumn<SectionData, String> idColumn = new TableColumn<>("ID");
        TableColumn<SectionData, String> versionColumn = new TableColumn<>("Version");
        TableColumn<SectionData, String> nameColumn = new TableColumn<>("Name");
        TableColumn<SectionData, String> subjectColumn = new TableColumn<>("Subject");
        TableColumn<SectionData, String> trimesterColumn = new TableColumn<>("Trimester");
        TableColumn<SectionData, String> capacityColumn = new TableColumn<>("Capacity");

        // Bind columns to SectionData properties
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        versionColumn.setCellValueFactory(data -> new SimpleStringProperty("1.0")); // Placeholder version value
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        subjectColumn.setCellValueFactory(data -> data.getValue().subjectProperty());
        trimesterColumn.setCellValueFactory(data -> data.getValue().trimesterProperty());
        capacityColumn.setCellValueFactory(data -> data.getValue().capacityProperty());

        // Add copy to clipboard for ID column
        idColumn.setCellFactory(col -> {
            TableCell<SectionData, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    javafx.scene.input.Clipboard.getSystemClipboard().setContent(new javafx.scene.input.ClipboardContent() {{
                        putString(cell.getItem());
                    }});
                    showNotification("Copied ID: " + cell.getItem(), "INFO");
                }
            });
            return cell;
        });

        // Configure table to match CoursePage
        tableView.setItems(sectionDataList);
        tableView.getColumns().clear();
        tableView.getColumns().addAll(idColumn, versionColumn, nameColumn, subjectColumn, trimesterColumn, capacityColumn);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DatabaseUiUtils.styleCourseTableView(tableView);

        // Row selection handler
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedIdLabel.setText("Selected ID: " + selected.getId());
                nameField.setText(selected.getName());
                subjectDropdown.setValue(selected.getSubject());
                trimesterDropdown.setValue(selected.getTrimester());
                maxCapacityField.setText(selected.getMaxCapacity());
            }
        });

        VBox container = new VBox(10, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return container;
    }

    private VBox createSectionForm() {
        // Initialize form components
        selectedIdLabel = new Label("No selection");
        Button clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setOnAction(e -> clearForm());

        HBox selectionControls = new HBox(10, selectedIdLabel, clearSelectionBtn);
        selectionControls.setAlignment(Pos.CENTER_LEFT);

        nameField = new TextField();
        subjectDropdown = new ComboBox<>(FXCollections.observableArrayList("Math", "Physics", "Chemistry"));
        trimesterDropdown = new ComboBox<>(FXCollections.observableArrayList("Spring 2024", "Summer 2024", "Fall 2024"));
        maxCapacityField = new TextField();

        VBox form = new VBox(10,
                selectionControls,
                new Label("Name:"), nameField,
                new Label("Subject:"), subjectDropdown,
                new Label("Trimester:"), trimesterDropdown,
                new Label("Max Capacity:"), maxCapacityField
        );

        VBox actions = DatabaseUiUtils.createActionButtons(
                this::createSection,
                this::updateSection,
                this::deleteSection,
                this::loadMockData
        );

        VBox container = new VBox(20, form, actions);
        container.setPadding(new Insets(20));
        return container;
    }

    private void clearForm() {
        tableView.getSelectionModel().clearSelection();
        selectedIdLabel.setText("No selection");
        nameField.clear();
        subjectDropdown.setValue(null);
        trimesterDropdown.setValue(null);
        maxCapacityField.clear();
    }

    private void createSection() {
        sectionDataList.add(new SectionData(
                UUID.randomUUID().toString(),
                nameField.getText(),
                subjectDropdown.getValue(),
                trimesterDropdown.getValue(),
                maxCapacityField.getText()
        ));
        clearForm();
    }

    private void updateSection() {
        SectionData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setName(nameField.getText());
            selected.setSubject(subjectDropdown.getValue());
            selected.setTrimester(trimesterDropdown.getValue());
            selected.setMaxCapacity(maxCapacityField.getText());
            tableView.refresh();
            clearForm();
        }
    }

    private void deleteSection() {
        SectionData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sectionDataList.remove(selected);
            clearForm();
        }
    }

    private void loadMockData() {
        sectionDataList.clear();
        for (int i = 0; i < 10; i++) {
            sectionDataList.add(new SectionData(
                    UUID.randomUUID().toString(),
                    faker.educator().course(),
                    faker.educator().campus(),
                    "Spring 2024",
                    String.valueOf(faker.number().numberBetween(20, 100))
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
    public static class SectionData {
        private final StringProperty id;
        private final StringProperty name;
        private final StringProperty subject;
        private final StringProperty trimester;
        private final StringProperty maxCapacity;

        public SectionData(String id, String name, String subject, String trimester, String maxCapacity) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.subject = new SimpleStringProperty(subject);
            this.trimester = new SimpleStringProperty(trimester);
            this.maxCapacity = new SimpleStringProperty(maxCapacity);
        }

        public StringProperty idProperty() {
            return id;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty subjectProperty() {
            return subject;
        }

        public StringProperty trimesterProperty() {
            return trimester;
        }

        public StringProperty capacityProperty() {
            return maxCapacity;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getName() {
            return name.get();
        }

        public void setSubject(String subject) {
            this.subject.set(subject);
        }

        public String getSubject() {
            return subject.get();
        }

        public void setTrimester(String trimester) {
            this.trimester.set(trimester);
        }

        public String getTrimester() {
            return trimester.get();
        }

        public void setMaxCapacity(String maxCapacity) {
            this.maxCapacity.set(maxCapacity);
        }

        public String getMaxCapacity() {
            return maxCapacity.get();
        }
    }
}
