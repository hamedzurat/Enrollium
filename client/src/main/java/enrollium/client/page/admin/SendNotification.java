package enrollium.client.page.admin;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class SendNotification extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SendNotification;

    public SendNotification() {
        super();

        addPageHeader();

        // Create the Notification Form UI
        VBox notificationSection = createNotificationForm();

        // Add the notification section to the page
        addSection("Send Notification", notificationSection);
    }

    private VBox createNotificationForm() {
        // Title field
        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();

        // Content field
        Label contentLabel = new Label("Content:");
        TextArea contentArea = new TextArea();
        contentArea.setWrapText(true);
        contentArea.setPromptText("Enter notification content here...");
        contentArea.setPrefHeight(80);
        contentArea.setPrefWidth(300);

        // Category dropdown
        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Info", "Warning", "Error");

        // Scope dropdown
        Label scopeLabel = new Label("Scope:");
        ComboBox<String> scopeComboBox = new ComboBox<>();
        scopeComboBox.getItems().addAll("Global", "Local");

        // Buttons
        Button createButton = new Button("Create");
        createButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        Button fillDemoButton = new Button("Fill for demo");

        HBox buttonBox = new HBox(10, createButton, updateButton, deleteButton, fillDemoButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout arrangement
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(20));
        formGrid.addRow(0, titleLabel, titleField);
        formGrid.addRow(1, contentLabel, contentArea);
        formGrid.addRow(2, categoryLabel, categoryComboBox);
        formGrid.addRow(3, scopeLabel, scopeComboBox);

        VBox notificationSection = new VBox(20, formGrid, buttonBox);
        notificationSection.setPadding(new Insets(20));
        notificationSection.setAlignment(Pos.TOP_CENTER);

        return notificationSection;
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
