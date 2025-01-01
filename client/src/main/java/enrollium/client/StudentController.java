package enrollium.client;

import com.fasterxml.jackson.databind.JsonNode;
import core.JsonUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class StudentController {
    private final ObservableList<StudentData>      students = FXCollections.observableArrayList();
    @FXML
    private       TextField                        nameField;
    @FXML
    private       TextField                        emailField;
    @FXML
    private       PasswordField                    passwordField;
    @FXML
    private       TableView<StudentData>           studentTable;
    @FXML
    private       TableColumn<StudentData, String> idColumn;
    @FXML
    private       TableColumn<StudentData, String> nameColumn;
    @FXML
    private       TableColumn<StudentData, String> emailColumn;
    @FXML
    private       TableColumn<StudentData, String> universityIdColumn;
    @FXML
    private       Label                            messageLabel;

    @FXML
    public void initialize() {
        setupTable();
        loadStudents();

        // Add selection listener
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                emailField.setText(newSelection.getEmail());
                passwordField.clear(); // Always clear password field on selection
            }
        });
    }

    private void setupTable() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        universityIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUniversityId()));

        studentTable.setItems(students);
    }

    @FXML
    public void loadStudents() {
        var params = JsonUtils.createObject();
        RPCManager.getInstance().getClient().call("getStudents", params).subscribe(response -> {
            if (!response.isError()) {
                Platform.runLater(() -> {
                    students.clear();
                    JsonNode studentsArray = response.getParams().get("students");
                    for (JsonNode student : studentsArray) {
                        students.add(new StudentData(student.get("id").asText(), student.get("name")
                                                                                        .asText(), student.get("email")
                                                                                                          .asText(), student.get("universityId")
                                                                                                                            .asText()));
                    }
                    updateMessage("Students loaded successfully");
                });
            } else {
                updateMessage("Error: " + response.getErrorMessage());
            }
        }, error -> updateMessage("Error: " + error.getMessage()));
    }

    @FXML
    private void handleCreate() {
        if (!validateInput(true)) return;

        var params = JsonUtils.createObject()
                              .put("name", nameField.getText().trim())
                              .put("email", emailField.getText().trim())
                              .put("password", passwordField.getText());

        RPCManager.getInstance().getClient().call("createStudent", params).subscribe(response -> {
            if (!response.isError()) {
                loadStudents();
                clearFields();
                updateMessage("Student created successfully");
            } else {
                updateMessage("Error: " + response.getErrorMessage());
            }
        }, error -> updateMessage("Error: " + error.getMessage()));
    }

    @FXML
    private void handleUpdate() {
        StudentData selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateMessage("Please select a student to update");
            return;
        }

        if (!validateInput(false)) return;

        var params = JsonUtils.createObject()
                              .put("id", selected.getId())
                              .put("name", nameField.getText().trim())
                              .put("email", emailField.getText().trim());

        RPCManager.getInstance().getClient().call("updateStudent", params).subscribe(response -> {
            if (!response.isError()) {
                loadStudents();
                clearFields();
                updateMessage("Student updated successfully");
            } else {
                updateMessage("Error: " + response.getErrorMessage());
            }
        }, error -> updateMessage("Error: " + error.getMessage()));
    }

    @FXML
    private void handleDelete() {
        StudentData selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateMessage("Please select a student to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Student");
        confirmation.setContentText("Are you sure you want to delete " + selected.getName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                var params = JsonUtils.createObject().put("id", selected.getId());
                RPCManager.getInstance().getClient().call("deleteStudent", params).subscribe(resp -> {
                    if (!resp.isError()) {
                        loadStudents();
                        clearFields();
                        updateMessage("Student deleted successfully");
                    } else {
                        updateMessage("Error: " + resp.getErrorMessage());
                    }
                }, error -> updateMessage("Error: " + error.getMessage()));
            }
        });
    }

    private boolean validateInput(boolean isCreate) {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (name.isEmpty() || email.isEmpty()) {
            updateMessage("Please fill in name and email fields");
            return false;
        }

        if (isCreate && password.isEmpty()) {
            updateMessage("Please provide a password for new student");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            updateMessage("Please enter a valid email address");
            return false;
        }

        return true;
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        studentTable.getSelectionModel().clearSelection();
    }

    private void updateMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    @Data
    public static class StudentData {
        private final String id;
        private final String name;
        private final String email;
        private final String universityId;
    }
}
