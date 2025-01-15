package enrollium.client.page.general;

import enrollium.client.page.OutlinePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;


public class ChangePass extends OutlinePage {
    public static final TranslationKey NAME = TranslationKey.CHANGEPASSWORD;

    public ChangePass() {
        super();

        // Add page header (as used in the Button page)
        addPageHeader();

        // Add the form section
        addSection("Changing Password", createForm());
    }

    private VBox createForm() {
        // Create layout for the form
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));

        // Add Old Password field
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Enter old password");
        oldPasswordField.setPrefWidth(250);

        // Add New Password field
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        newPasswordField.setPrefWidth(250);

        // Add Confirm Password field
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setPrefWidth(250);

        // Add Submit Button
        Button submitButton = new Button("Change Password");
        submitButton.setOnAction(event -> {
            String oldPassword     = oldPasswordField.getText();
            String newPassword     = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Error", "All fields must be filled.");
            } else if (!newPassword.equals(confirmPassword)) {
                showAlert("Error", "New password and confirmation do not match.");
            } else {
                showAlert("Success", "Password changed successfully!");
            }
        });

        // Add all components to the form
        form.getChildren().addAll(oldPasswordField, newPasswordField, confirmPasswordField, submitButton);

        return form;
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    protected void updateTexts() {
        // Update dynamic texts if necessary
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
