package enrollium.client.page.home;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class Chat extends BasePage {
    public static final TranslationKey NAME = TranslationKey.Chat;

    public Chat() {
        super();

        addPageHeader();
        TabPane tabPane = createTabPane();

        addNode(tabPane);
    }

    private TabPane createTabPane() {
        // Create TabPane
        TabPane classicTabs = new TabPane();
        classicTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        classicTabs.setMinWidth(450);

        // Create "Create New" Tab
        Tab       createNewTab  = new Tab("Create New");
        VBox      createNewPane = new VBox(10); // Vertical layout with spacing of 10
        Label     emailLabel    = new Label("Email:");
        TextField emailField    = new TextField();
        Button    startButton   = new Button("Start");
        startButton.setOnAction(event -> {
            String email = emailField.getText();
            if (email.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Email cannot be empty!");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Chat Started", "Chat started with: " + email);
            }
        });
        createNewPane.getChildren().addAll(emailLabel, emailField, startButton);
        createNewTab.setContent(createNewPane);

        // Create "Chats" Tab
        Tab        chatsTab  = new Tab("Chats");
        BorderPane chatsPane = new BorderPane();
        TextArea   chatArea  = new TextArea();
        chatArea.setEditable(false); // Make chat area read-only
        chatArea.setWrapText(true);  // Enable word wrapping

        TextField messageField = new TextField();
        Button    sendButton   = new Button("Send");
        sendButton.setOnAction(event -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                chatArea.appendText("You: " + message + "\n");
                messageField.clear();
            }
        });

        HBox messageBox = new HBox(10, messageField, sendButton); // Horizontal layout with spacing of 10
        chatsPane.setCenter(new ScrollPane(chatArea)); // Scrollable chat area
        chatsPane.setBottom(messageBox); // Add message box to the bottom
        chatsTab.setContent(chatsPane);

        // Add Tabs to TabPane
        classicTabs.getTabs().addAll(createNewTab, chatsTab);

        return classicTabs;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
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
