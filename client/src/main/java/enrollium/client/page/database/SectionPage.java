package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import net.datafaker.Faker;
import javafx.geometry.Insets;


import java.util.UUID;

public class SectionPage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SECTION;
    private final TableView<SectionData> tableView = new TableView<>();
    private final ObservableList<SectionData> sectionDataList = FXCollections.observableArrayList();
    private final Faker faker = new Faker();
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
        TableColumn<SectionData, String> nameColumn = new TableColumn<>("Name");
        TableColumn<SectionData, String> subjectColumn = new TableColumn<>("Subject");
        TableColumn<SectionData, String> trimesterColumn = new TableColumn<>("Trimester");
        TableColumn<SectionData, String> capacityColumn = new TableColumn<>("Capacity");

        // Bind columns to SectionData properties
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        subjectColumn.setCellValueFactory(data -> data.getValue().subjectProperty());
        trimesterColumn.setCellValueFactory(data -> data.getValue().trimesterProperty());
        capacityColumn.setCellValueFactory(data -> data.getValue().capacityProperty());

        tableView.getColumns().addAll(idColumn, nameColumn, subjectColumn, trimesterColumn, capacityColumn);
        tableView.setItems(sectionDataList);

        // Row click handling for table
        tableView.setRowFactory(tv -> {
            TableRow<SectionData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    SectionData rowData = row.getItem();
                    nameField.setText(rowData.getName());
                    subjectDropdown.setValue(rowData.getSubject());
                    trimesterDropdown.setValue(rowData.getTrimester());
                    maxCapacityField.setText(rowData.getMaxCapacity());
                }
            });
            return row;
        });

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createSectionForm() {
        // Initialize form fields
        nameField = new TextField();
        subjectDropdown = new ComboBox<>(FXCollections.observableArrayList("Math", "Physics", "Chemistry"));
        trimesterDropdown = new ComboBox<>(FXCollections.observableArrayList("Spring 2024", "Summer 2024", "Fall 2024"));
        maxCapacityField = new TextField();

        // Create action buttons
        VBox form = new VBox(10,
                new Label("Name:"), nameField,
                new Label("Subject:"), subjectDropdown,
                new Label("Trimester:"), trimesterDropdown,
                new Label("Max Capacity:"), maxCapacityField,
                DatabaseUiUtils.createActionButtons(
                        // Create Action
                        () -> {
                            sectionDataList.add(new SectionData(
                                    UUID.randomUUID().toString(),
                                    nameField.getText(),
                                    subjectDropdown.getValue(),
                                    trimesterDropdown.getValue(),
                                    maxCapacityField.getText()));
                            clearForm();
                        },
                        // Update Action
                        () -> {
                            SectionData selected = tableView.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                selected.setName(nameField.getText());
                                selected.setSubject(subjectDropdown.getValue());
                                selected.setTrimester(trimesterDropdown.getValue());
                                selected.setMaxCapacity(maxCapacityField.getText());
                                tableView.refresh();
                                clearForm();
                            }
                        },
                        // Delete Action
                        () -> {
                            SectionData selected = tableView.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                sectionDataList.remove(selected);
                                clearForm();
                            }
                        },
                        // Fill for Demo Action
                        this::loadMockData
                )
        );
        form.setPadding(new Insets(10));
        return form;
    }

    private void clearForm() {
        nameField.clear();
        subjectDropdown.setValue(null);
        trimesterDropdown.setValue(null);
        maxCapacityField.clear();
    }

    private void loadMockData() {
        sectionDataList.clear();
        for (int i = 0; i < 10; i++) {
            sectionDataList.add(new SectionData(
                    UUID.randomUUID().toString(),
                    faker.educator().course(),
                    faker.educator().campus(),
                    "Spring 2024",
                    String.valueOf(faker.number().numberBetween(20, 100))));
        }
    }

    // SectionData class definition
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

        public String getId() {
            return id.get();
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
