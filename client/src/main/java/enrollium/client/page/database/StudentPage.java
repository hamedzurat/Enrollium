package enrollium.client.page.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enrollium.client.page.BasePage;
import enrollium.client.page.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.rpc.client.ClientRPC;
import enrollium.rpc.core.JsonUtils;
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

import java.util.Random;


public class StudentPage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.STUDENT;

    private final TableView<StudentData> tableView = new TableView<>();
    private final ObservableList<StudentData> studentDataList = FXCollections.observableArrayList();

    private final TableColumn<StudentData, String> idColumn = new TableColumn<>("ID");
    private final TableColumn<StudentData, String> universityIdColumn = new TableColumn<>("University ID");
    private final TableColumn<StudentData, String> nameColumn = new TableColumn<>("Name");
    private final TableColumn<StudentData, String> emailColumn = new TableColumn<>("Email");

    private Label selectedIdLabel;
    private TextField universityIdField;
    private TextField nameField;
    private TextField emailField;

    public StudentPage() {
        super();
        try {
            if (ClientRPC.getInstance() == null) {
                throw new IllegalStateException("ClientRPC must be initialized before accessing StudentPage.");
            }
            addPageHeader();
            addFormattedText("Manage student profiles with university IDs and personal information.");
            addSection("Students Table", createStudentTable());
            addSection("Student Form", createStudentForm());
            loadData();
        } catch (Exception e) {
            showNotification("Error initializing StudentPage: " + e.getMessage(), NotificationType.DANGER);
            throw new RuntimeException("Failed to initialize StudentPage", e);
        }
    }

    private VBox createStudentTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        universityIdColumn.setCellValueFactory(data -> data.getValue().universityIdProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());

        // Set preferred column widths
        idColumn.setPrefWidth(150);
        universityIdColumn.setPrefWidth(200);
        nameColumn.setPrefWidth(250);
        emailColumn.setPrefWidth(300);

        // Automatically resize columns to fill the available space
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add columns to the table
        tableView.setItems(studentDataList);
        tableView.getColumns().addAll(idColumn, universityIdColumn, nameColumn, emailColumn);

        // Set table dimensions
        tableView.setMinHeight(400);
        tableView.setMinWidth(900);

        // Add selection listener
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedIdLabel.setText("Selected ID: " + selected.getId());
                universityIdField.setText(selected.getUniversityId());
                nameField.setText(selected.getName());
                emailField.setText(selected.getEmail());
            }
        });

        VBox container = new VBox(10, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        container.setPadding(new Insets(20));
        return container;
    }

    private VBox createStudentForm() {
        selectedIdLabel = new Label("No selection");
        Button clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setOnAction(e -> clearForm());

        HBox selectionControls = new HBox(10, selectedIdLabel, clearSelectionBtn);
        selectionControls.setAlignment(Pos.CENTER_LEFT);

        universityIdField = new TextField();
        nameField = new TextField();
        emailField = new TextField();

        VBox form = new VBox(10,
                selectionControls,
                new Label("University ID:"), universityIdField,
                new Label("Name:"), nameField,
                new Label("Email:"), emailField
        );

        VBox actions = DatabaseUiUtils.createActionButtons(this::createStudent, this::updateStudent, this::deleteStudent, this::fillDemoData);

        VBox container = new VBox(20, form, actions);
        container.setPadding(new Insets(20));
        return container;
    }

    private void clearForm() {
        tableView.getSelectionModel().clearSelection();
        selectedIdLabel.setText("No selection");
        universityIdField.clear();
        nameField.clear();
        emailField.clear();
    }

    private void loadData() {
        try {
            ClientRPC.getInstance().call("Student.list", JsonUtils.createObject()).subscribe(response -> {
                JsonNode params = response.getParams();
                JsonNode items = params != null ? params.get("items") : null;

                if (items != null && items.isArray()) {
                    Platform.runLater(() -> {
                        studentDataList.clear();
                        for (JsonNode item : items) {
                            String id = item.hasNonNull("id") ? item.get("id").asText() : generateRandomId();
                            String universityId = item.hasNonNull("universityId") ? item.get("universityId").asText() : generateUniversityId();
                            String name = item.hasNonNull("name") ? item.get("name").asText() : "N/A";
                            String email = item.hasNonNull("email") ? item.get("email").asText() : generateEmail(name);

                            studentDataList.add(new StudentData(id, universityId, name, email));
                        }
                    });
                } else {
                    Platform.runLater(() -> showNotification("No students found or invalid response format", NotificationType.WARNING));
                }
            }, error -> showNotification("Failed to load students: " + error.getMessage(), NotificationType.DANGER));
        } catch (IllegalStateException e) {
            showNotification("ClientRPC is not initialized: " + e.getMessage(), NotificationType.DANGER);
        }
    }

    private String generateUniversityId() {
        Random random    = new Random();
        int    trimester = random.nextInt(899) + 100; // Random number between 100-999 for trimester
        int randomNum = random.nextInt(8999) + 1000; // Random number between 1000-9999
        return "011" + trimester + randomNum; // Concatenate with 011 prefix
    }

    private String generateEmail(String name) {
        Random random = new Random();
        int trimester = random.nextInt(899) + 100; // Random number between 100-999 for trimester
        int randomNum = random.nextInt(8999) + 1000; // Random number between 1000-9999

        // Split name into parts
        String[] nameParts = name.split(" ");
        String firstNamePart = nameParts.length > 0 ? nameParts[0].substring(0, Math.min(3, nameParts[0].length())).toLowerCase() : "stu";
        String secondNamePart = nameParts.length > 1 ? nameParts[1].substring(0, Math.min(3, nameParts[1].length())).toLowerCase() : "dent";

        return firstNamePart + secondNamePart + trimester + randomNum + "@bscse.uiu.ac.bd"; // Concatenate components
    }

    private String generateRandomId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }


    private void createStudent() {
        if (universityIdField.getText().isEmpty() || nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showNotification("All fields are required", NotificationType.WARNING);
            return;
        }

        ObjectNode params = JsonUtils.createObject()
                                     .put("universityId", universityIdField.getText())
                                     .put("name", nameField.getText())
                                     .put("email", emailField.getText());

        ClientRPC.getInstance().call("Student.create", params).subscribe(response -> {
            Platform.runLater(() -> {
                showNotification("Student created successfully", NotificationType.SUCCESS);
                loadData();
                clearForm();
            });
        }, error -> showNotification("Failed to create student: " + error.getMessage(), NotificationType.DANGER));
    }

    private void updateStudent() {
        StudentData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Please select a student to update", NotificationType.WARNING);
            return;
        }

        if (universityIdField.getText().isEmpty() || nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showNotification("All fields are required", NotificationType.WARNING);
            return;
        }

        ObjectNode params = JsonUtils.createObject()
                                     .put("id", selected.getId())
                                     .put("universityId", universityIdField.getText())
                                     .put("name", nameField.getText())
                                     .put("email", emailField.getText());

        ClientRPC.getInstance().call("Student.update", params).subscribe(response -> {
            Platform.runLater(() -> {
                showNotification("Student updated successfully", NotificationType.SUCCESS);
                loadData();
                clearForm();
            });
        }, error -> showNotification("Failed to update student: " + error.getMessage(), NotificationType.DANGER));
    }

    private void deleteStudent() {
        StudentData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Please select a student to delete", NotificationType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Student");
        alert.setHeaderText("Delete Student");
        alert.setContentText("Are you sure you want to delete this student?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                JsonNode params = JsonUtils.createObject().put("id", selected.getId());

                ClientRPC.getInstance().call("Student.delete", params).subscribe(result -> {
                    Platform.runLater(() -> {
                        showNotification("Student deleted successfully", NotificationType.SUCCESS);
                        loadData();
                        clearForm();
                    });
                }, error -> showNotification("Failed to delete student: " + error.getMessage(), NotificationType.DANGER));
            }
        });
    }

    private void fillDemoData() {
        universityIdField.setText("U123456");
        nameField.setText("John Doe");
        emailField.setText("john.doe@example.com");
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private static class StudentData {
        private final SimpleStringProperty id;
        private final SimpleStringProperty universityId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;

        public StudentData(String id, String universityId, String name, String email) {
            this.id = new SimpleStringProperty(id);
            this.universityId = new SimpleStringProperty(universityId);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
        }

        public StringProperty idProperty() {
            return id;
        }

        public StringProperty universityIdProperty() {
            return universityId;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty emailProperty() {
            return email;
        }

        public String getId() {
            return id.get();
        }

        public String getUniversityId() {
            return universityId.get();
        }

        public String getName() {
            return name.get();
        }

        public String getEmail() {
            return email.get();
        }
    }
}
