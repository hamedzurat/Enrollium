package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.datafaker.Faker;

import java.util.UUID;


public class NotificationPage extends BasePage {
    public static final TranslationKey                        NAME                 = TranslationKey.NOTIFICATION;
    private final       TableView<NotificationData>           tableView            = new TableView<>();
    private final       ObservableList<NotificationData>      notificationDataList = FXCollections.observableArrayList();
    private final       Faker                                 faker                = new Faker();
    private final       TableColumn<NotificationData, String> idColumn             = new TableColumn<>("ID");
    private final       TableColumn<NotificationData, String> titleColumn          = new TableColumn<>("Title");
    private final       TableColumn<NotificationData, String> categoryColumn       = new TableColumn<>("Category");
    private final       TableColumn<NotificationData, String> scopeColumn          = new TableColumn<>("Scope");
    private             TextField                             titleField;
    private             TextArea                              contentArea;
    private             ComboBox<String>                      categoryDropdown;
    private             ComboBox<String>                      scopeDropdown;

    public NotificationPage() {
        super();
        addPageHeader();
        addFormattedText("Manage notifications with title, content, category, and scope.");
        addSection("Notifications Table", createNotificationTable());
        addSection("Notification Form", createNotificationForm());
        loadMockData();
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createNotificationTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        scopeColumn.setCellValueFactory(data -> data.getValue().scopeProperty());

        idColumn.setCellFactory(tc -> {
            TableCell<NotificationData, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem());
                    Clipboard.getSystemClipboard().setContent(content);
                }
            });
            return cell;
        });

        tableView.getColumns().addAll(idColumn, titleColumn, categoryColumn, scopeColumn);
        tableView.setItems(notificationDataList);
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

        DatabaseUiUtils.styleCourseTableView(tableView);
        notificationDataList.addListener((InvalidationListener) change -> DatabaseUiUtils.adjustTableHeight(tableView));

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createNotificationForm() {
        titleField       = new TextField();
        contentArea      = new TextArea();
        categoryDropdown = new ComboBox<>(FXCollections.observableArrayList("URGENT", "ACADEMIC", "ADMINISTRATIVE", "GENERAL"));
        scopeDropdown    = new ComboBox<>(FXCollections.observableArrayList("GLOBAL", "TRIMESTER", "SECTION", "USER"));

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("Title:"), titleField, new Label("Content:"), contentArea, new Label("Category:"), categoryDropdown, new Label("Scope:"), scopeDropdown, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            notificationDataList.add(new NotificationData(UUID.randomUUID()
                                                              .toString(), titleField.getText(), contentArea.getText(), categoryDropdown.getValue(), scopeDropdown.getValue()));
        });

        updateBtn.setOnAction(e -> {
            NotificationData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setTitle(titleField.getText());
                selected.setContent(contentArea.getText());
                selected.setCategory(categoryDropdown.getValue());
                selected.setScope(scopeDropdown.getValue());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            NotificationData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                notificationDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            notificationDataList.add(new NotificationData(UUID.randomUUID().toString(), faker.lorem()
                                                                                             .sentence(), faker.lorem()
                                                                                                               .paragraph(), faker.options()
                                                                                                                                  .option("URGENT", "ACADEMIC", "GENERAL"), faker.options()
                                                                                                                                                                                 .option("GLOBAL", "TRIMESTER", "USER")));
        }
    }
}
