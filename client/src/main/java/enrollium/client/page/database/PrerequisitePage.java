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

import java.util.UUID;

public class PrerequisitePage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.PREREQUISITE;

    // Table components
    private final TableView<PrerequisiteData> tableView = new TableView<>();
    private final ObservableList<PrerequisiteData> prerequisiteDataList = FXCollections.observableArrayList();
    private final ObservableList<String> subjectList = FXCollections.observableArrayList("Math", "Physics", "Chemistry", "Biology", "Computer Science");
    private final Faker faker = new Faker();

    // Form components
    private Label selectedIdLabel;
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
        TableColumn<PrerequisiteData, String> versionColumn = new TableColumn<>("Version");
        TableColumn<PrerequisiteData, String> subjectColumn = new TableColumn<>("Subject");
        TableColumn<PrerequisiteData, String> prerequisiteColumn = new TableColumn<>("Prerequisite");
        TableColumn<PrerequisiteData, String> minGradeColumn = new TableColumn<>("Minimum Grade");

        // Bind columns to PrerequisiteData properties
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        versionColumn.setCellValueFactory(data -> new SimpleStringProperty("1.0")); // Placeholder version value
        subjectColumn.setCellValueFactory(data -> data.getValue().subjectProperty());
        prerequisiteColumn.setCellValueFactory(data -> data.getValue().prerequisiteProperty());
        minGradeColumn.setCellValueFactory(data -> data.getValue().minimumGradeProperty());

        // Add copy to clipboard for ID column
        idColumn.setCellFactory(col -> {
            TableCell<PrerequisiteData, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    Clipboard.getSystemClipboard().setContent(new ClipboardContent() {{
                        putString(cell.getItem());
                    }});
                    showNotification("Copied ID: " + cell.getItem(), "INFO");
                }
            });
            return cell;
        });

        // Configure table to match CoursePage
        tableView.setItems(prerequisiteDataList);
        tableView.getColumns().clear();
        tableView.getColumns().addAll(idColumn, versionColumn, subjectColumn, prerequisiteColumn, minGradeColumn);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DatabaseUiUtils.styleCourseTableView(tableView);

        // Row selection handler
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedIdLabel.setText("Selected ID: " + selected.getId());
                subjectDropdown.setValue(selected.getSubject().get());
                prerequisiteDropdown.setValue(selected.getPrerequisite().get());
                minGradeField.setText(selected.getMinimumGrade().get());
            }
        });

        VBox container = new VBox(10, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return container;
    }

    private VBox createPrerequisiteForm() {
        // Initialize form components
        selectedIdLabel = new Label("No selection");
        Button clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setOnAction(e -> clearForm());

        HBox selectionControls = new HBox(10, selectedIdLabel, clearSelectionBtn);
        selectionControls.setAlignment(Pos.CENTER_LEFT);

        subjectDropdown = new ComboBox<>(subjectList);
        prerequisiteDropdown = new ComboBox<>(subjectList);
        minGradeField = new TextField();

        // Grade validation
        minGradeField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                minGradeField.setText(old);
            }
        });

        VBox form = new VBox(10,
                selectionControls,
                new Label("Subject:"), subjectDropdown,
                new Label("Prerequisite Subject:"), prerequisiteDropdown,
                new Label("Minimum Grade (1.33 - 4.00):"), minGradeField
        );

        VBox actions = DatabaseUiUtils.createActionButtons(
                this::createPrerequisite,
                this::updatePrerequisite,
                this::deletePrerequisite,
                this::loadMockData
        );

        VBox container = new VBox(20, form, actions);
        container.setPadding(new Insets(20));
        return container;
    }

    private void clearForm() {
        tableView.getSelectionModel().clearSelection();
        selectedIdLabel.setText("No selection");
        subjectDropdown.setValue(null);
        prerequisiteDropdown.setValue(null);
        minGradeField.clear();
    }

    private void createPrerequisite() {
        String gradeText = minGradeField.getText();
        double grade;
        try {
            grade = Double.parseDouble(gradeText);
            if (grade < 1.33 || grade > 4.00) {
                showNotification("Minimum grade must be between 1.33 and 4.00", "WARNING");
                return;
            }
        } catch (NumberFormatException e) {
            showNotification("Invalid grade format", "WARNING");
            return;
        }

        prerequisiteDataList.add(new PrerequisiteData(
                UUID.randomUUID().toString(),
                subjectDropdown.getValue(),
                prerequisiteDropdown.getValue(),
                gradeText
        ));
        clearForm();
    }

    private void updatePrerequisite() {
        PrerequisiteData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String gradeText = minGradeField.getText();
            double grade;
            try {
                grade = Double.parseDouble(gradeText);
                if (grade < 1.33 || grade > 4.00) {
                    showNotification("Minimum grade must be between 1.33 and 4.00", "WARNING");
                    return;
                }
            } catch (NumberFormatException e) {
                showNotification("Invalid grade format", "WARNING");
                return;
            }

            selected.setSubject(subjectDropdown.getValue());
            selected.setPrerequisite(prerequisiteDropdown.getValue());
            selected.setMinimumGrade(gradeText);
            tableView.refresh();
            clearForm();
        }
    }

    private void deletePrerequisite() {
        PrerequisiteData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            prerequisiteDataList.remove(selected);
            clearForm();
        }
    }

    private void loadMockData() {
        prerequisiteDataList.clear();
        for (int i = 0; i < 10; i++) {
            double grade = 1.33 + faker.random().nextDouble() * (4.00 - 1.33);
            prerequisiteDataList.add(new PrerequisiteData(
                    UUID.randomUUID().toString(),
                    faker.educator().course(),
                    faker.educator().course(),
                    String.format("%.2f", grade)
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

        public void setSubject(String value) {
            this.subject.set(value);
        }

        public void setPrerequisite(String value) {
            this.prerequisite.set(value);
        }

        public void setMinimumGrade(String gradeText) {
            this.minimumGrade.set(gradeText);
        }
    }
}
