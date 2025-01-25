package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import net.datafaker.Faker;

import java.util.UUID;

public class NotificationPage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.NOTIFICATION;
    private final TableView<NotificationData> tableView = new TableView<>();
    private final ObservableList<NotificationData> notificationDataList = FXCollections.observableArrayList();
    private final Faker faker = new Faker();
    private TextField titleField;
    private TextArea contentArea;
    private ComboBox<String> categoryDropdown;
    private ComboBox<String> scopeDropdown;

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
        // Define table columns
        TableColumn<NotificationData, String> idColumn = new TableColumn<>("ID");
        TableColumn<NotificationData, String> titleColumn = new TableColumn<>("Title");
        TableColumn<NotificationData, String> contentColumn = new TableColumn<>("Content");
        TableColumn<NotificationData, String> categoryColumn = new TableColumn<>("Category");
        TableColumn<NotificationData, String> scopeColumn = new TableColumn<>("Scope");

        // Bind columns to NotificationData properties
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        contentColumn.setCellValueFactory(data -> data.getValue().contentProperty());
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        scopeColumn.setCellValueFactory(data -> data.getValue().scopeProperty());

        // Add columns to the table
        tableView.getColumns().addAll(idColumn, titleColumn, contentColumn, categoryColumn, scopeColumn);
        tableView.setItems(notificationDataList);

        // Handle row clicks to populate the form
        tableView.setRowFactory(tv -> {
            TableRow<NotificationData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    NotificationData rowData = row.getItem();
                    titleField.setText(rowData.getTitle());
                    contentArea.setText(rowData.getContent());
                    categoryDropdown.setValue(rowData.getCategory());
                    scopeDropdown.setValue(rowData.getScope());
                }
            });
            return row;
        });

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createNotificationForm() {
        // Initialize form fields
        titleField = new TextField();
        contentArea = new TextArea();
        categoryDropdown = new ComboBox<>(FXCollections.observableArrayList("URGENT", "ACADEMIC", "ADMINISTRATIVE", "GENERAL"));
        scopeDropdown = new ComboBox<>(FXCollections.observableArrayList("GLOBAL", "TRIMESTER", "SECTION", "USER"));

        // Create action buttons using DatabaseUiUtils
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
                new Label("Title:"), titleField,
                new Label("Content:"), contentArea,
                new Label("Category:"), categoryDropdown,
                new Label("Scope:"), scopeDropdown,
                actionButtons
        );
        form.setPadding(new Insets(10));
        return form;
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
