package enrollium.client;

import client.ClientRPC;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.JsonUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StudentController {
    @FXML private TextField studentIdField;
    @FXML private Button createButton;
    @FXML private Button getButton;
    @FXML private Label resultLabel;

    private final ClientRPC client = new ClientRPC("test", "test123");
    private String lastCreatedId = null;

    @FXML
    public void initialize() {
        try {
            client.start();
            resultLabel.setText("Connected to server");
        } catch (Exception e) {
            log.error("Failed to connect to server", e);
            resultLabel.setText("Failed to connect: " + e.getMessage());
            createButton.setDisable(true);
            getButton.setDisable(true);
        }
    }

    @FXML
    private void handleCreate() {
        client.call("createStudent", JsonUtils.createObject())
              .subscribe(
                      response -> {
                          if (!response.isError()) {
                              JsonNode params = response.getParams();
                              lastCreatedId = params.get("id").asText();
                              updateUI("Created student: " + params.get("name").asText() +
                                       "\nID: " + lastCreatedId);
                          } else {
                              updateUI("Error: " + response.getErrorMessage());
                          }
                      },
                      error -> updateUI("Error: " + error.getMessage())
              );
    }

    @FXML
    private void handleGet() {
        String studentId = studentIdField.getText().trim();
        if (studentId.isEmpty()) {
            resultLabel.setText("Please enter a student ID");
            return;
        }

        ObjectNode params = JsonUtils.createObject();
        params.put("studentId", studentId);

        client.call("getStudent", params)
              .subscribe(
                      response -> {
                          if (!response.isError()) {
                              JsonNode student = response.getParams();
                              updateUI("Found student:\n" +
                                       "Name: " + student.get("name").asText() + "\n" +
                                       "University ID: " + student.get("universityId").asText() + "\n" +
                                       "Email: " + student.get("email").asText());
                          } else {
                              updateUI("Error: " + response.getErrorMessage());
                          }
                      },
                      error -> updateUI("Error: " + error.getMessage())
              );
    }

    private void updateUI(String message) {
        Platform.runLater(() -> resultLabel.setText(message));
    }

    public void shutdown() {
        client.close();
    }
}
