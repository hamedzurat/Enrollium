package enrollium.client.page.database;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enrollium.client.page.BasePage;
import enrollium.client.page.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.memory.Volatile;
import enrollium.rpc.client.ClientRPC;
import enrollium.rpc.core.JsonUtils;
import enrollium.server.db.entity.types.CourseStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Data;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;


public class CoursePage extends BasePage {
    public static final TranslationKey                        NAME            = TranslationKey.COURSE;
    // Table components
    private final       TableView<CourseData>                 tableView       = new TableView<>();
    private final       ObservableList<CourseData>            courseDataList  = FXCollections.observableArrayList();
    // Table columns
    private final       TableColumn<CourseData, String>       idColumn        = new TableColumn<>("ID");
    private final       TableColumn<CourseData, Long>         versionColumn   = new TableColumn<>("Version");
    private final       TableColumn<CourseData, CourseStatus> statusColumn    = new TableColumn<>("Status");
    private final       TableColumn<CourseData, String>       studentColumn   = new TableColumn<>("Student");
    private final       TableColumn<CourseData, String>       subjectColumn   = new TableColumn<>("Subject");
    private final       TableColumn<CourseData, Integer>      trimesterColumn = new TableColumn<>("Trimester");
    private final       TableColumn<CourseData, String>       sectionColumn   = new TableColumn<>("Section");
    private final       TableColumn<CourseData, Double>       gradeColumn     = new TableColumn<>("Grade");
    // Data lists for dropdowns
    private final       ObservableList<DropdownItem>          studentList     = FXCollections.observableArrayList();
    private final       ObservableList<DropdownItem>          subjectList     = FXCollections.observableArrayList();
    private final       ObservableList<DropdownItem>          trimesterList   = FXCollections.observableArrayList();
    private final       ObservableList<DropdownItem>          sectionList     = FXCollections.observableArrayList();
    private final       Volatile                              memory          = Volatile.getInstance();
    // Form components
    private             Label                                 selectedIdLabel;
    private             ComboBox<CourseStatus>                statusDropdown;
    private             ComboBox<DropdownItem>                studentDropdown;
    private             ComboBox<DropdownItem>                subjectDropdown;
    private             ComboBox<DropdownItem>                trimesterDropdown;
    private             ComboBox<DropdownItem>                sectionDropdown;
    private             TextField                             gradeField;
    private             Message                               statusMessage;

    public CoursePage() {
        super();
        addPageHeader();
        addFormattedText(TranslationKey.COURSE_desc);

        String userId   = (String) memory.get("auth_user_id");
        String userType = (String) memory.get("auth_user_type");

        if (userId == null || userType == null) {
            Message msg = new Message("Error", "User information not found. Please log first.", new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
            msg.getStyleClass().add(Styles.DANGER);
            addNode(msg);
        } else if (!userType.equals("ADMIN")) {
            Message msg = new Message("Error", "Only System Administrator can access this page.", new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
            msg.getStyleClass().add(Styles.DANGER);
            addNode(msg);
        } else {
            addSection(TranslationKey.COURSE_table, createCourseTable());
            addSection(TranslationKey.COURSE_form, createCourseForm());
            loadData();
        }
    }

    private VBox createCourseTable() {
        // Setup columns
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        versionColumn.setCellValueFactory(data -> data.getValue().versionProperty().asObject());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        studentColumn.setCellValueFactory(data -> data.getValue().studentNameProperty());
        subjectColumn.setCellValueFactory(data -> data.getValue().subjectNameProperty());
        trimesterColumn.setCellValueFactory(data -> data.getValue().trimesterCodeProperty().asObject());
        sectionColumn.setCellValueFactory(data -> data.getValue().sectionNameProperty());
        gradeColumn.setCellValueFactory(data -> data.getValue().gradeProperty());

        // Add copy to clipboard for ID column
        idColumn.setCellFactory(col -> {
            TableCell<CourseData, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    javafx.scene.input.Clipboard.getSystemClipboard()
                                                .setContent(new javafx.scene.input.ClipboardContent() {{
                                                    putString(cell.getItem());
                                                }});
                    showNotification("Copied ID: " + cell.getItem(), NotificationType.INFO);
                }
            });
            return cell;
        });

        // Configure table
        tableView.setItems(courseDataList);
        tableView.getColumns()
                 .addAll(idColumn, versionColumn, statusColumn, studentColumn, subjectColumn, trimesterColumn, sectionColumn, gradeColumn);

        // Row selection handler
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedIdLabel.setText("Selected ID: " + selected.getId());
                statusDropdown.setValue(selected.getStatus());
                studentDropdown.getItems()
                               .stream()
                               .filter(item -> item.getId().equals(selected.getStudentId()))
                               .findFirst()
                               .ifPresent(studentDropdown::setValue);
                subjectDropdown.getItems()
                               .stream()
                               .filter(item -> item.getId().equals(selected.getSubjectId()))
                               .findFirst()
                               .ifPresent(subjectDropdown::setValue);
                trimesterDropdown.getItems()
                                 .stream()
                                 .filter(item -> item.getId().equals(selected.getTrimesterId()))
                                 .findFirst()
                                 .ifPresent(trimesterDropdown::setValue);
                if (selected.getSectionId() != null) {
                    sectionDropdown.getItems()
                                   .stream()
                                   .filter(item -> item.getId().equals(selected.getSectionId()))
                                   .findFirst()
                                   .ifPresent(sectionDropdown::setValue);
                }
                if (selected.getGrade() != null) {
                    gradeField.setText(String.format("%.2f", selected.getGrade()));
                } else {
                    gradeField.clear();
                }
                updateFormState();
            }
        });

        DatabaseUiUtils.styleCourseTableView(tableView);

        VBox container = new VBox(10, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return container;
    }

    private VBox createCourseForm() {
        // Initialize form components
        selectedIdLabel = new Label("No selection");
        Button clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setOnAction(e -> clearForm());

        HBox selectionControls = new HBox(10, selectedIdLabel, clearSelectionBtn);
        selectionControls.setAlignment(Pos.CENTER_LEFT);

        statusDropdown    = new ComboBox<>(FXCollections.observableArrayList(CourseStatus.values()));
        studentDropdown   = new ComboBox<>(studentList);
        subjectDropdown   = new ComboBox<>(subjectList);
        trimesterDropdown = new ComboBox<>(trimesterList);
        sectionDropdown   = new ComboBox<>(sectionList);
        gradeField        = new TextField();

        // Grade validation
        gradeField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                gradeField.setText(old);
            }
        });

        // Section dropdown updates based on subject and trimester
        subjectDropdown.valueProperty().addListener((obs, old, newVal) -> updateSectionDropdown());
        trimesterDropdown.valueProperty().addListener((obs, old, newVal) -> updateSectionDropdown());

        // Status change handler
        statusDropdown.valueProperty().addListener((obs, old, newVal) -> updateFormState());

        VBox form = new VBox(10, selectionControls, new Label("Status:"), statusDropdown, new Label("Student:"), studentDropdown, new Label("Subject:"), subjectDropdown, new Label("Trimester:"), trimesterDropdown, new Label("Section:"), sectionDropdown, new Label("Grade:"), gradeField);

        VBox actions = DatabaseUiUtils.createActionButtons(this::createCourse, this::updateCourse, this::deleteCourse, this::fillDemoData);

        VBox container = new VBox(20, form, actions);
        container.setPadding(new Insets(20));
        return container;
    }

    private void updateFormState() {
        boolean isCompleted = statusDropdown.getValue() == CourseStatus.COMPLETED;
        gradeField.setDisable(!isCompleted);
        if (!isCompleted) {
            gradeField.clear();
        }
    }

    private void clearForm() {
        tableView.getSelectionModel().clearSelection();
        selectedIdLabel.setText("No selection");
        statusDropdown.setValue(null);
        studentDropdown.setValue(null);
        subjectDropdown.setValue(null);
        trimesterDropdown.setValue(null);
        sectionDropdown.setValue(null);
        gradeField.clear();
    }

    private void updateSectionDropdown() {
        DropdownItem subject   = subjectDropdown.getValue();
        DropdownItem trimester = trimesterDropdown.getValue();

        if (subject != null && trimester != null) {
            loadSections(subject.getId(), trimester.getId());
        } else {
            sectionDropdown.getItems().clear();
        }
    }

    private void loadData() {
        // Load courses
        ClientRPC.getInstance()
                 .call("Course.getAll", JsonUtils.createObject().put("limit", 1000).put("offset", 0))
                 .subscribe(response -> {
                     JsonNode items = response.getParams().get("items");
                     Platform.runLater(() -> {
                         courseDataList.clear();
                         for (JsonNode item : items) {
                             CourseData data = new CourseData();
                             // Set properties from JSON
                             data.setId(item.get("id").asText());
                             data.setVersion(item.get("version").asLong());
                             data.setStatus(CourseStatus.valueOf(item.get("status").asText()));
                             data.setStudentId(item.get("studentId").asText());
                             data.setStudentName(item.get("studentName").asText());
                             data.setSubjectId(item.get("subjectId").asText());
                             data.setSubjectName(item.get("subjectName").asText());
                             data.setTrimesterId(item.get("trimesterId").asText());
                             data.setTrimesterCode(item.get("trimesterCode").asInt());

                             if (item.has("sectionId")) {
                                 data.setSectionId(item.get("sectionId").asText());
                                 data.setSectionName(item.get("sectionName").asText());
                             }
                             if (item.has("grade")) {
                                 data.setGrade(item.get("grade").asDouble());
                             }
                             courseDataList.add(data);
                         }
                     });
                 }, error -> showNotification("Failed to load courses: " + error.getMessage(), NotificationType.DANGER));

        // Load dropdown data
        loadStudents();
        loadSubjects();
        loadTrimesters();
    }

    private void loadStudents() {
        ClientRPC.getInstance().call("Student.list", JsonUtils.createObject()).subscribe(response -> {
            JsonNode items = response.getParams().get("items");
            Platform.runLater(() -> {
                studentList.clear();
                for (JsonNode item : items) {
                    studentList.add(new DropdownItem(item.get("id").asText(), item.get("name").asText()));
                }
            });
        }, error -> showNotification("Failed to load students: " + error.getMessage(), NotificationType.DANGER));
    }

    private void loadTrimesters() {
        ClientRPC.getInstance().call("Trimester.list", JsonUtils.createObject()).subscribe(response -> {
            JsonNode items = response.getParams().get("items");
            Platform.runLater(() -> {
                trimesterList.clear();
                for (JsonNode item : items) {
                    trimesterList.add(new DropdownItem(item.get("id").asText(), "Code: " + item.get("code").asText()));
                }
            });
        }, error -> showNotification("Failed to load trimesters: " + error.getMessage(), NotificationType.DANGER));
    }

    private void loadSections(String subjectId, String trimesterId) {
        JsonNode params = JsonUtils.createObject().put("subjectId", subjectId).put("trimesterId", trimesterId);

        ClientRPC.getInstance().call("Section.list", params).subscribe(response -> {
            JsonNode items = response.getParams().get("items");
            Platform.runLater(() -> {
                sectionList.clear();
                for (JsonNode item : items) {
                    sectionList.add(new DropdownItem(item.get("id").asText(), item.get("name").asText()));
                }
            });
        }, error -> showNotification("Failed to load sections: " + error.getMessage(), NotificationType.DANGER));
    }

    private void loadSubjects() {
        ClientRPC.getInstance().call("Subject.list", JsonUtils.createObject()).subscribe(response -> {
            JsonNode items = response.getParams().get("items");
            Platform.runLater(() -> {
                subjectList.clear();
                for (JsonNode item : items) {
                    subjectList.add(new DropdownItem(item.get("id").asText(), item.get("name").asText()));
                }
            });
        }, error -> showNotification("Failed to load subjects: " + error.getMessage(), NotificationType.DANGER));
    }

    private void createCourse() {
        // Validation
        if (statusDropdown.getValue() == null || studentDropdown.getValue() == null || subjectDropdown.getValue() == null || trimesterDropdown.getValue() == null || sectionDropdown.getValue() == null) {
            showNotification("Please fill all required fields", NotificationType.WARNING);
            return;
        }

        // Create request object
        ObjectNode params = JsonUtils.createObject()
                                     .put("studentId", studentDropdown.getValue().getId())
                                     .put("subjectId", subjectDropdown.getValue().getId())
                                     .put("trimesterId", trimesterDropdown.getValue().getId())
                                     .put("sectionId", sectionDropdown.getValue().getId())
                                     .put("status", statusDropdown.getValue().toString());

        if (statusDropdown.getValue() == CourseStatus.COMPLETED && !gradeField.getText().isEmpty()) {
            try {
                double grade = Double.parseDouble(gradeField.getText());
                if (grade < 0.0 || grade > 4.0) {
                    showNotification("Grade must be between 0.0 and 4.0", NotificationType.WARNING);
                    return;
                }
                params.put("grade", grade);
            } catch (NumberFormatException e) {
                showNotification("Invalid grade format", NotificationType.WARNING);
                return;
            }
        }

        // Send create request
        ClientRPC.getInstance().call("Course.create", params).subscribe(response -> {
            Platform.runLater(() -> {
                showNotification("Course created successfully", NotificationType.SUCCESS);
                loadData();
                clearForm();
            });
        }, error -> showNotification("Failed to create course: " + error.getMessage(), NotificationType.DANGER));
    }

    private void updateCourse() {
        CourseData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Please select a course to update", NotificationType.WARNING);
            return;
        }

        // Required fields validation
        if (statusDropdown.getValue() == null || studentDropdown.getValue() == null || subjectDropdown.getValue() == null || trimesterDropdown.getValue() == null) {
            showNotification("All fields are required except grade", NotificationType.WARNING);
            return;
        }

        // Section validation for non-SELECTED status
        if (statusDropdown.getValue() != CourseStatus.SELECTED && sectionDropdown.getValue() == null) {
            showNotification("Section is required for non-SELECTED status", NotificationType.WARNING);
            return;
        }

        // Create request object
        ObjectNode params = JsonUtils.createObject()
                                     .put("id", selected.getId())
                                     .put("studentId", studentDropdown.getValue().getId())
                                     .put("subjectId", subjectDropdown.getValue().getId())
                                     .put("trimesterId", trimesterDropdown.getValue().getId())
                                     .put("status", statusDropdown.getValue().toString());

        // Add section if present
        if (sectionDropdown.getValue() != null) {
            params.put("sectionId", sectionDropdown.getValue().getId());
        }

        // Add grade if status is COMPLETED
        if (statusDropdown.getValue() == CourseStatus.COMPLETED) {
            if (gradeField.getText().isEmpty()) {
                showNotification("Grade is required for COMPLETED status", NotificationType.WARNING);
                return;
            }
            try {
                double grade = Double.parseDouble(gradeField.getText());
                if (grade < 0.0 || grade > 4.0) {
                    showNotification("Grade must be between 0.0 and 4.0", NotificationType.WARNING);
                    return;
                }
                params.put("grade", grade);
            } catch (NumberFormatException e) {
                showNotification("Invalid grade format", NotificationType.WARNING);
                return;
            }
        }

        ClientRPC.getInstance().call("Course.update", params).subscribe(response -> {
            Platform.runLater(() -> {
                showNotification("Course updated successfully", NotificationType.SUCCESS);
                loadData();
                clearForm();
            });
        }, error -> showNotification("Failed to update course: " + error.getMessage(), NotificationType.DANGER));
    }

    private void deleteCourse() {
        CourseData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Please select a course to delete", NotificationType.WARNING);
            return;
        }

        // Create confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Course");
        alert.setHeaderText("Delete Course");
        alert.setContentText("Are you sure you want to delete this course?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                JsonNode params = JsonUtils.createObject().put("id", selected.getId());

                ClientRPC.getInstance().call("Course.delete", params).subscribe(result -> {
                    Platform.runLater(() -> {
                        showNotification("Course deleted successfully", NotificationType.SUCCESS);
                        loadData();
                        clearForm();
                    });
                }, error -> showNotification("Failed to delete course: " + error.getMessage(), NotificationType.DANGER));
            }
        });
    }

    private void fillDemoData() {
        if (studentDropdown.getItems().isEmpty() || subjectDropdown.getItems().isEmpty() || trimesterDropdown.getItems()
                                                                                                             .isEmpty()) {
            showNotification("Please wait for data to load", NotificationType.WARNING);
            return;
        }

        statusDropdown.setValue(CourseStatus.SELECTED);
        studentDropdown.setValue(studentDropdown.getItems().get(0));
        subjectDropdown.setValue(subjectDropdown.getItems().get(0));
        trimesterDropdown.setValue(trimesterDropdown.getItems().get(0));
        // Section and grade will be handled by the listeners
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    @Data
    private static class DropdownItem {
        private final String id;
        private final String displayText;

        @Override
        public String toString() {
            return displayText;
        }
    }
}
