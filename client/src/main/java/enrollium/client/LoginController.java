package enrollium.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LoginController {
    @FXML
    private TextField     emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label         messageLabel;

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both email and password");
            return;
        }

        try {
            RPCManager.getInstance().initializeConnection(email, password);
            openStudentWindow();
            closeLoginWindow();
        } catch (Exception e) {
            log.error("Login failed", e);
            messageLabel.setText("Login failed: " + e.getMessage());
        }
    }

    private void openStudentWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/enrollium/client/student.fxml"));
            Parent     root   = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Student Management");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            log.error("Failed to open student window", e);
            messageLabel.setText("Failed to open student window: " + e.getMessage());
        }
    }

    private void closeLoginWindow() {
        Platform.runLater(() -> {
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.close();
        });
    }
}
