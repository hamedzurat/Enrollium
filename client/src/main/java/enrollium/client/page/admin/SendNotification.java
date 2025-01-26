package enrollium.client.page.admin;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

public class SendNotification extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SendNotification;

    public SendNotification() {
        super();

        addPageHeader();

        // Create the Send Notification UI
        VBox notificationSection = createNotificationSection();

        // Add the notification section to the page
        addSection("Send Notification", notificationSection);
    }

    private VBox createNotificationSection() {
        Label typeLabel = new Label("Type:");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Info", "Warning", "Error");

        Label scopeLabel = new Label("Scope:");
        ComboBox<String> scopeComboBox = new ComboBox<>();
        scopeComboBox.getItems().addAll("Global", "Local");

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();

        Label contentLabel = new Label("Content:");
        TextArea contentArea = new TextArea();
        contentArea.setWrapText(true);
        contentArea.setPromptText("Enter notification content here...");
        contentArea.setMaxSize(300, 120);
        contentArea.setMinSize(300, 120);

        Button sendButton = new Button("Send");

        VBox notificationSection = new VBox(10,
                typeLabel, typeComboBox,
                scopeLabel, scopeComboBox,
                titleLabel, titleField,
                contentLabel, contentArea,
                sendButton);
        notificationSection.setPadding(new Insets(20));

        return notificationSection;
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
