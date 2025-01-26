package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.client.page.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.datafaker.Faker;

import java.util.UUID;

public class NotificationPage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.NOTIFICATION;

    // Table components
    private final TableView<NotificationData> tableView = new TableView<>();
    private final ObservableList<NotificationData> notificationDataList = FXCollections.observableArrayList();
    private final TableColumn<NotificationData, String> idColumn = new TableColumn<>("ID");
    private final TableColumn<NotificationData, String> titleColumn = new TableColumn<>("Title");
    private final TableColumn<NotificationData, String> contentColumn = new TableColumn<>("Content");
    private final TableColumn<NotificationData, String> categoryColumn = new TableColumn<>("Category");
    private final TableColumn<NotificationData, String> scopeColumn = new TableColumn<>("Scope");
    private final Faker faker = new Faker();

    // Form components
    private TextField titleField;
    private TextArea contentArea;
    private ComboBox<String> categoryDropdown;
    private ComboBox<String> scopeDropdown;
    private Label selectedIdLabel;

    public NotificationPage() {
        super();
        addPageHeader();
        addFormattedText("Manage notifications with title, content, category, and scope.");
        addSection("Notifications Table", createNotificationTable());
        addSection("Notification Form", createNotificationForm());
        loadMockData();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createNotificationTable() {
        // Setup columns
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        contentColumn.setCellValueFactory(data -> data.getValue().contentProperty());
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        scopeColumn.setCellValueFactory(data -> data.getValue().scopeProperty());

        // Configure ID column for copy-to-clipboard functionality
        idColumn.setCellFactory(col -> {
            TableCell<NotificationData, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    String itemId = cell.getItem();
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(itemId);
                    clipboard.setContent(content);
                    showNotification("Copied ID: " + itemId, NotificationType.INFO);
                }
            });
            return cell;
        });

        // Configure table
        tableView.setItems(notificationDataList);
        tableView.getColumns().addAll(idColumn, titleColumn, contentColumn, categoryColumn, scopeColumn);

        // Row selection handler
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedIdLabel.setText("Selected ID: " + selected.getId());
                titleField.setText(selected.getTitle());
                contentArea.setText(selected.getContent());
                categoryDropdown.setValue(selected.getCategory());
                scopeDropdown.setValue(selected.getScope());
            }
        });

        // Apply styling if necessary
        DatabaseUiUtils.styleCourseTableView(tableView);

        VBox container = new VBox(10, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createNotificationForm() {
        // Initialize form fields
        selectedIdLabel = new Label("No selection");
        Button clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setOnAction(e -> clearForm());

        titleField = new TextField();
        contentArea = new TextArea();
        categoryDropdown = new ComboBox<>(FXCollections.observableArrayList("URGENT", "ACADEMIC", "ADMINISTRATIVE", "GENERAL"));
        scopeDropdown = new ComboBox<>(FXCollections.observableArrayList("GLOBAL", "TRIMESTER", "SECTION", "USER"));

        VBox selectionControls = new VBox(10, selectedIdLabel, clearSelectionBtn);

        // Create action buttons
        VBox actionButtons = DatabaseUiUtils.createActionButtons(
                // Create Action
                () -> notificationDataList.add(new NotificationData(
                        UUID.randomUUID().toString(),
                        titleField.getText(),
                        contentArea.getText(),
                        categoryDropdown.getValue(),
                        scopeDropdown.getValue())),
                // Update Action
                () -> {
                    NotificationData selected = tableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        selected.setTitle(titleField.getText());
                        selected.setContent(contentArea.getText());
                        selected.setCategory(categoryDropdown.getValue());
                        selected.setScope(scopeDropdown.getValue());
                        tableView.refresh();
                    }
                },
                // Delete Action
                () -> {
                    NotificationData selected = tableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        notificationDataList.remove(selected);
                    }
                },
                // Fill for Demo Action
                this::loadMockData
        );

        VBox form = new VBox(10,
                selectionControls,
                new Label("Title:"), titleField,
                new Label("Content:"), contentArea,
                new Label("Category:"), categoryDropdown,
                new Label("Scope:"), scopeDropdown,
                actionButtons
        );
        form.setPadding(new Insets(10));
        return form;
    }

    private void clearForm() {
        tableView.getSelectionModel().clearSelection();
        selectedIdLabel.setText("No selection");
        titleField.clear();
        contentArea.clear();
        categoryDropdown.setValue(null);
        scopeDropdown.setValue(null);
    }

    private void loadMockData() {
        notificationDataList.clear();
        for (int i = 0; i < 10; i++) {
            notificationDataList.add(new NotificationData(
                    UUID.randomUUID().toString(),
                    faker.lorem().sentence(),
                    faker.lorem().paragraph(),
                    faker.options().option("URGENT", "ACADEMIC", "GENERAL"),
                    faker.options().option("GLOBAL", "TRIMESTER", "USER")));
        }
    }

    // NotificationData class definition
    public static class NotificationData {
        private final StringProperty id;
        private final StringProperty title;
        private final StringProperty content;
        private final StringProperty category;
        private final StringProperty scope;

        public NotificationData(String id, String title, String content, String category, String scope) {
            this.id = new SimpleStringProperty(id);
            this.title = new SimpleStringProperty(title);
            this.content = new SimpleStringProperty(content);
            this.category = new SimpleStringProperty(category);
            this.scope = new SimpleStringProperty(scope);
        }

        public StringProperty idProperty() {
            return id;
        }

        public StringProperty titleProperty() {
            return title;
        }

        public StringProperty contentProperty() {
            return content;
        }

        public StringProperty categoryProperty() {
            return category;
        }

        public StringProperty scopeProperty() {
            return scope;
        }

        public String getId() {
            return id.get();
        }

        public void setTitle(String title) {
            this.title.set(title);
        }

        public String getTitle() {
            return title.get();
        }

        public void setContent(String content) {
            this.content.set(content);
        }

        public String getContent() {
            return content.get();
        }

        public void setCategory(String category) {
            this.category.set(category);
        }

        public String getCategory() {
            return category.get();
        }

        public void setScope(String scope) {
            this.scope.set(scope);
        }

        public String getScope() {
            return scope.get();
        }
    }
}
