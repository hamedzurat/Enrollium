package enrollium.client.page.home;

import enrollium.client.page.BasePage;
import enrollium.client.page.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.memory.Volatile;
import enrollium.rpc.client.ClientRPC;
import enrollium.rpc.core.JsonUtils;
import io.reactivex.rxjava3.core.Single;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Chat extends BasePage {
    public static final TranslationKey        NAME         = TranslationKey.Chat;
    private final       Volatile              storage      = Volatile.getInstance();
    private final       UserColorManager      colorManager = new UserColorManager();
    private             ListView<ChatMessage> chatListView;
    private             TextField             messageInput;
    private             ComboBox<String>      emailSelector;
    private             TabPane               tabPane;
    private             Tab                   chatTab;

    public Chat() {
        super();
        initializeStorage();
        createPageStructure();
        startListening();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private void initializeStorage() {
        if (!storage.containsKey("chatHistories")) {
            storage.put("chatHistories", new HashMap<String, ObservableList<ChatMessage>>());
        }
    }

    private void createPageStructure() {
        addPageHeader();

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab setupTab = createSetupTab();
        chatTab = createChatTab();

        tabPane.getTabs().addAll(setupTab, chatTab);
        chatTab.setDisable(true);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.getChildren().add(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        addNode(container);
    }

    private Tab createSetupTab() {
        TextField emailInput = new TextField();
        emailInput.setPromptText("Enter your email");

        Button startButton = new Button("Start Chat");

        VBox setupContent = new VBox(20);
        setupContent.setAlignment(Pos.CENTER);
        setupContent.getChildren()
                    .addAll(createFormattedText("[b]Welcome to Enrollium Chat[/b]", true), emailInput, startButton);

        startButton.setOnAction(e -> handleStartChat(emailInput.getText()));

        return createTab("Setup", setupContent);
    }

    private Tab createChatTab() {
        emailSelector = new ComboBox<>();
        emailSelector.setPromptText("Select a chat...");
        populateEmailSelector();

        chatListView = new ListView<>();
        chatListView.setCellFactory(param -> new ChatListCell());
        chatListView.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(chatListView, Priority.ALWAYS);

        messageInput = new TextField();
        messageInput.setPromptText("Type your message...");

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());

        HBox inputBox = new HBox(10);
        inputBox.getChildren().addAll(messageInput, sendButton);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        VBox chatContent = new VBox(10);
        chatContent.getChildren().addAll(createHeader(), chatListView, inputBox);
        chatContent.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(chatListView, Priority.ALWAYS);

        emailSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                chatListView.setItems(getChatHistory(newVal));
                chatListView.scrollTo(chatListView.getItems().size() - 1);
            }
        });

        Tab tab = new Tab("Chat");
        tab.setContent(chatContent);
//        StackPane.setVgrow(chatContent, Priority.ALWAYS);
        return tab;
    }

    private Node createHeader() {
        HBox header = new HBox(10);
        header.getChildren().addAll(emailSelector, createFormattedText("[b]Active Chat[/b]", true));
        return header;
    }

    private void handleStartChat(String email) {
        if (!email.isEmpty()) {
            addChatHistory(email);
            chatTab.setDisable(false);
            tabPane.getSelectionModel().select(1);
            showNotification("Chat session started", NotificationType.SUCCESS);
            addSystemMessage(email, "Chat started as " + email);
        }
    }

    private void sendMessage() {
        String recipientEmail = emailSelector.getValue();
        String message        = messageInput.getText().trim();

        if (!message.isEmpty() && recipientEmail != null) {
            ClientRPC.getInstance()
                     .call("sendMessage", JsonUtils.createObject()
                                                   .put("recipient", recipientEmail)
                                                   .put("message", message))
                     .subscribe(response -> {
                         getChatHistory(recipientEmail).add(new ChatMessage("Me", message, Color.LIGHTBLUE));
                         messageInput.clear();
                     }, error -> {
                         showNotification("Failed to send message: " + error.getMessage(), NotificationType.WARNING);
                     });
        }
    }

    private void startListening() {
        ClientRPC.getInstance().registerMethod("receiveMessage", params -> {
            try {
                String senderEmail = JsonUtils.getString(params, "sender");
                String message     = JsonUtils.getString(params, "message");

                System.out.println(message);

                // Run all UI-related operations inside Platform.runLater()
                Platform.runLater(() -> {
                    try {
                        // Store messages using the sender's email as the key
                        getChatHistory(senderEmail).add(new ChatMessage(senderEmail, message, Color.LIGHTGREEN));

                        // Add sender email to the dropdown if not already present
                        if (!emailSelector.getItems().contains(senderEmail)) {
                            emailSelector.getItems().add(senderEmail);
                        }

                        chatListView.scrollTo(chatListView.getItems().size() - 1);
                    } catch (Exception e) {
                        showNotification("Error updating UI: " + e.getMessage(), NotificationType.WARNING);
                    }
                });

                return Single.just(JsonUtils.createObject().put("status", "ok"));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Failed to process message: " + e.getMessage()));
            }
        });
    }

    private void addSystemMessage(String email, String message) {
        getChatHistory(email).add(new ChatMessage("System", message, Color.LIGHTGREEN));
    }

    @SuppressWarnings("unchecked")
    private Map<String, ObservableList<ChatMessage>> getChatHistories() {
        return (Map<String, ObservableList<ChatMessage>>) storage.get("chatHistories");
    }

    private ObservableList<ChatMessage> getChatHistory(String email) {
        String key = "chat_" + email;

        if (!Volatile.getInstance().containsKey(key)) {
            Volatile.getInstance().put(key, FXCollections.observableArrayList());
        }

        return (ObservableList<ChatMessage>) Volatile.getInstance().get(key);
    }

    private void addChatHistory(String email) {
        Map<String, ObservableList<ChatMessage>> histories = getChatHistories();
        if (!histories.containsKey(email)) {
            histories.put(email, FXCollections.observableArrayList());
            emailSelector.getItems().add(email);
        }
        emailSelector.getSelectionModel().select(email);
        emailSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                chatListView.setItems(getChatHistory(newVal));
                chatListView.scrollTo(chatListView.getItems().size() - 1);
            }
        });
    }

    private void populateEmailSelector() {
        emailSelector.getItems().addAll(getChatHistories().keySet());
    }

    private void mockRpcCall(String email, String message) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    getChatHistory(email).add(new ChatMessage("Server", "Received '" + message + "'", Color.LIGHTBLUE));
                    chatListView.scrollTo(chatListView.getItems().size() - 1);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private Tab createTab(String title, Pane content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }

    private record ChatMessage(String sender, String content, Color color) {}


    private static class UserColorManager {
        private final Map<String, Color> userColors = new HashMap<>();
        private final Random             random     = new Random();

        public Color getUserColor(String email) {
            return userColors.computeIfAbsent(email, k -> Color.hsb(random.nextDouble() * 360, 0.7, 0.8));
        }
    }


    private class ChatListCell extends ListCell<ChatMessage> {
        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                Platform.runLater(() -> {
                    String currentEmail = emailSelector.getValue();
                    String senderDisplay = item.sender().equals(currentEmail)
                                           ? "[ME]"
                                           : item.sender().equals("Server") ? "Server" : item.sender();

                    Label userLabel    = new Label(senderDisplay + ": ");
                    Label messageLabel = new Label(item.content());
                    userLabel.setTextFill(item.color());

                    HBox container = new HBox(userLabel, messageLabel);
                    container.setSpacing(5);
                    setGraphic(container);
                });
            }
        }
    }
}
