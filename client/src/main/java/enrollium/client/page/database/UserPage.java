package enrollium.client.page.database;

import enrollium.client.page.OutlinePage;
import enrollium.design.system.i18n.TranslationKey;
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


public class UserPage extends OutlinePage {
    public static final TranslationKey           NAME         = TranslationKey.USER;
    private final       TableView<UserData>      tableView    = new TableView<>();
    private final       ObservableList<UserData> userDataList = FXCollections.observableArrayList();
    private final       Faker                    faker        = new Faker();
    private             TextField                nameField;
    private             TextField                emailField;
    private             ComboBox<String>         roleDropdown;

    public UserPage() {
        super();
        addPageHeader();
        addFormattedText("Manage user profiles with roles and login information.");
        addSection("Users Table", createUserTable());
        addSection("User Form", createUserForm());
        loadMockData();
    }

    @Override
    protected void updateTexts() {}

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createUserTable() {
        TableColumn<UserData, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        idColumn.setCellFactory(tc -> {
            TableCell<UserData, String> cell = new TableCell<>() {
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

        TableColumn<UserData, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<UserData, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<UserData, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(data -> data.getValue().roleProperty());

        tableView.getColumns().addAll(idColumn, nameColumn, emailColumn, roleColumn);
        tableView.setItems(userDataList);
        tableView.setRowFactory(tv -> {
            TableRow<UserData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    UserData rowData = row.getItem();
                    nameField.setText(rowData.getName());
                    emailField.setText(rowData.getEmail());
                    roleDropdown.setValue(rowData.getRole());
                }
            });
            return row;
        });

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createUserForm() {
        nameField    = new TextField();
        emailField   = new TextField();
        roleDropdown = new ComboBox<>(FXCollections.observableArrayList("STUDENT", "TEACHER", "ADMIN"));

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("Name:"), nameField, new Label("Email:"), emailField, new Label("Role:"), roleDropdown, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            userDataList.add(new UserData(UUID.randomUUID()
                                              .toString(), nameField.getText(), emailField.getText(), roleDropdown.getValue()));
        });

        updateBtn.setOnAction(e -> {
            UserData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setName(nameField.getText());
                selected.setEmail(emailField.getText());
                selected.setRole(roleDropdown.getValue());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            UserData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                userDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            userDataList.add(new UserData(UUID.randomUUID().toString(), faker.name().fullName(), faker.internet()
                                                                                                      .emailAddress(), faker.options()
                                                                                                                            .option("STUDENT", "TEACHER", "ADMIN")));
        }
    }
}
