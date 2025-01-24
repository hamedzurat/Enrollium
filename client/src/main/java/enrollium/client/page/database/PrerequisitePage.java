package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import net.datafaker.Faker;

import java.util.UUID;

public class PrerequisitePage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.PREREQUISITE;
    private final TableView<PrerequisiteData> tableView = new TableView<>();
    private final ObservableList<PrerequisiteData> prerequisiteDataList = FXCollections.observableArrayList();
    private final ObservableList<String> subjectList = FXCollections.observableArrayList("Math", "Physics", "Chemistry", "Biology", "Computer Science");
    private final Faker faker = new Faker();
    private ComboBox<String> subjectDropdown;
    private ComboBox<String> prerequisiteDropdown;
    private TextField minGradeField;

    public PrerequisitePage() {
        super();
        addPageHeader();
        addFormattedText("Manage subject prerequisites with minimum grade requirements.");
        addSection("Prerequisites Table", createPrerequisiteTable());
        addSection("Prerequisite Form", createPrerequisiteForm());
        loadMockData();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createPrerequisiteTable() {
        // Define table columns
        TableColumn<PrerequisiteData, String> idColumn = new TableColumn<>("ID");
        TableColumn<PrerequisiteData, String> subjectColumn = new TableColumn<>("Subject");
        TableColumn<PrerequisiteData, String> prerequisiteColumn = new TableColumn<>("Prerequisite");
        TableColumn<PrerequisiteData, String> minGradeColumn = new TableColumn<>("Minimum Grade");

        // Bind columns to PrerequisiteData properties
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        subjectColumn.setCellValueFactory(data -> data.getValue().subjectProperty());
        prerequisiteColumn.setCellValueFactory(data -> data.getValue().prerequisiteProperty());
        minGradeColumn.setCellValueFactory(data -> data.getValue().minimumGradeProperty());

        // Add columns to the table
        tableView.getColumns().addAll(idColumn, subjectColumn, prerequisiteColumn, minGradeColumn);
        tableView.setItems(prerequisiteDataList);

        // Handle row clicks to populate the form
        tableView.setRowFactory(tv -> {
            TableRow<PrerequisiteData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    PrerequisiteData rowData = row.getItem();
                    subjectDropdown.setValue(rowData.getSubject());
                    prerequisiteDropdown.setValue(rowData.getPrerequisite());
                    minGradeField.setText(rowData.getMinimumGrade());
                }
            });
            return row;
        });

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createPrerequisiteForm() {
        // Initialize form fields
        subjectDropdown = new ComboBox<>(subjectList);
        prerequisiteDropdown = new ComboBox<>(subjectList);
        minGradeField = new TextField();

        // Create action buttons using DatabaseUiUtils
        VBox actionButtons = DatabaseUiUtils.createActionButtons(
                // Create Action
                () -> {
                    String gradeText = minGradeField.getText();
                    double grade;
                    try {
                        grade = Double.parseDouble(gradeText);
                        if (grade < 1.33 || grade > 4.00) {
                            showAlert("Error", "Minimum grade must be between 1.33 and 4.00.");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Invalid grade. Please enter a number between 1.33 and 4.00.");
                        return;
                    }

                    prerequisiteDataList.add(new PrerequisiteData(
                            UUID.randomUUID().toString(),
                            subjectDropdown.getValue(),
                            prerequisiteDropdown.getValue(),
                            gradeText));
                },
                // Update Action
                () -> {
                    PrerequisiteData selected = tableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        String gradeText = minGradeField.getText();
                        double grade;
                        try {
                            grade = Double.parseDouble(gradeText);
                            if (grade < 1.33 || grade > 4.00) {
                                showAlert("Error", "Minimum grade must be between 1.33 and 4.00.");
                                return;
                            }
                        } catch (NumberFormatException e) {
                            showAlert("Error", "Invalid grade. Please enter a number between 1.33 and 4.00.");
                            return;
                        }

                        selected.setSubject(subjectDropdown.getValue());
                        selected.setPrerequisite(prerequisiteDropdown.getValue());
                        selected.setMinimumGrade(gradeText);
                        tableView.refresh();
                    }
                },
                // Delete Action
                () -> {
                    PrerequisiteData selected = tableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        prerequisiteDataList.remove(selected);
                    }
                },
                // Fill for Demo Action
                this::loadMockData
        );

        VBox form = new VBox(10,
                new Label("Subject:"), subjectDropdown,
                new Label("Prerequisite Subject:"), prerequisiteDropdown,
                new Label("Minimum Grade (1.33 - 4.00):"), minGradeField,
                actionButtons
        );
        form.setPadding(new Insets(10));
        return form;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadMockData() {
        prerequisiteDataList.clear();
        for (int i = 0; i < 10; i++) {
            double grade = 1.33 + faker.random().nextDouble() * (4.00 - 1.33); // Generate random grades between 1.33 and 4.00
            prerequisiteDataList.add(new PrerequisiteData(
                    UUID.randomUUID().toString(),
                    faker.educator().course(),
                    faker.educator().course(),
                    String.format("%.2f", grade)));
        }
    }

    // PrerequisiteData class definition
    public static class PrerequisiteData {
        private final StringProperty id;
        private final StringProperty subject;
        private final StringProperty prerequisite;
        private final StringProperty minimumGrade;

        public PrerequisiteData(String id, String subject, String prerequisite, String minimumGrade) {
            this.id = new SimpleStringProperty(id);
            this.subject = new SimpleStringProperty(subject);
            this.prerequisite = new SimpleStringProperty(prerequisite);
            this.minimumGrade = new SimpleStringProperty(minimumGrade);
        }

        public StringProperty idProperty() {
            return id;
        }

        public StringProperty subjectProperty() {
            return subject;
        }

        public StringProperty prerequisiteProperty() {
            return prerequisite;
        }

        public StringProperty minimumGradeProperty() {
            return minimumGrade;
        }

        public String getId() {
            return id.get();
        }

        public String getSubject() {
            return subject.get();
        }

        public void setSubject(String subject) {
            this.subject.set(subject);
        }

        public String getPrerequisite() {
            return prerequisite.get();
        }

        public void setPrerequisite(String prerequisite) {
            this.prerequisite.set(prerequisite);
        }

        public String getMinimumGrade() {
            return minimumGrade.get();
        }

        public void setMinimumGrade(String minimumGrade) {
            this.minimumGrade.set(minimumGrade);
        }
    }
}
