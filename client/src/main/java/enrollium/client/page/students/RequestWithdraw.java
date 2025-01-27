package enrollium.client.page.students;

import atlantafx.base.controls.Card;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RequestWithdraw extends BasePage {

    public static final TranslationKey NAME = TranslationKey.RequestWithdraw;

    public RequestWithdraw() {
        super();

        addPageHeader(); // Add a header to the page
        setHeaderTitle("Course Withdrawal Request"); // Set the header title

        // Create main content layout
        VBox content = new VBox();
        content.setSpacing(20); // Add spacing between components
        content.setPadding(new Insets(20)); // Add padding around the content
        content.setAlignment(Pos.TOP_CENTER); // Align content to the center

        // Add Withdrawal Form Section
        Card withdrawFormCard = createWithdrawFormCard();

        // Add content to the layout
        content.getChildren().addAll(withdrawFormCard);

        // Add content to the main layout
        addNode(content);
    }

    private void setHeaderTitle(String title) {
        // Placeholder for setting the header title
    }

    /**
     * Creates a card containing the withdrawal request form.
     *
     * @return Card containing the withdrawal request form
     */
    private Card createWithdrawFormCard() {
        Card formCard = new Card();
        formCard.setMinWidth(400); // Adjust the card width

        VBox formBox = new VBox();
        formBox.setSpacing(15); // Space between form elements
        formBox.setPadding(new Insets(15)); // Add padding inside the card
        formBox.setAlignment(Pos.CENTER_LEFT); // Align content to the left

        Label formHeader = new Label("Withdrawal Form");
        formHeader.setFont(javafx.scene.text.Font.font(16)); // Set font size for headers

        // Course Selection Dropdown
        Label courseLabel = new Label("Select Course:");
        ComboBox<String> courseDropdown = new ComboBox<>();
        courseDropdown.getItems().addAll(
                "English–I",
                "Discrete Mathematics",
                "Structured Programming Language",
                "Fundamental Calculus",
                "Calculus and Linear Algebra",
                "Digital Logic Design",
                "Object Oriented Programming",
                "Data Structures and Algorithms",
                "Physics",
                "Electrical Circuits",
                "History of the Emergence of Bangladesh",
                "Coordinate Geometry and Vector Analysis",
                "Statistics for Engineers",
                "Introduction to Machine Learning",
                "Database Management Systems",
                "Computer Networks",
                "Operating Systems"
        );

        // Reason for Withdrawal
        Label reasonLabel = new Label("Reason for Withdrawal:");
        TextArea reasonField = new TextArea();
        reasonField.setPromptText("Enter your reason for withdrawal...");
        reasonField.setWrapText(true);
        reasonField.setPrefHeight(100); // Set height for the text area

        // Submit Button
        Button submitButton = new Button("Submit Request");
        submitButton.setOnAction(e -> handleWithdrawRequest(courseDropdown, reasonField));

        // Cancel Button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> handleCancel());

        HBox buttonBox = new HBox(10, submitButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        formBox.getChildren().addAll(formHeader, courseLabel, courseDropdown, reasonLabel, reasonField, buttonBox);
        formCard.setBody(formBox);

        return formCard;
    }

    /**
     * Handles the withdrawal request submission.
     *
     * @param courseDropdown The dropdown for selecting the course
     * @param reasonField    The text area for entering the reason
     */
    private void handleWithdrawRequest(ComboBox<String> courseDropdown, TextArea reasonField) {
        String selectedCourse = courseDropdown.getValue();
        String reason = reasonField.getText();

        if (selectedCourse == null || reason.isEmpty()) {
            showAlert("Error", "Please select a course and provide a reason for withdrawal.");
        } else {
            showAlert("Success", "Your withdrawal request for " + selectedCourse + " has been submitted.");
            courseDropdown.getSelectionModel().clearSelection();
            reasonField.clear();
        }
    }

    /**
     * Handles the cancel action.
     */
    private void handleCancel() {
        showAlert("Cancelled", "Your withdrawal request has been cancelled.");
    }

    /**
     * Shows an alert dialog.
     *
     * @param title   The title of the alert
     * @param message The message of the alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
