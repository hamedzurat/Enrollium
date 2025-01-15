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


public class FacultyPage extends OutlinePage {
    public static final TranslationKey              NAME            = TranslationKey.FACULTY;
    private final       TableView<FacultyData>      tableView       = new TableView<>();
    private final       ObservableList<FacultyData> facultyDataList = FXCollections.observableArrayList();
    private final       Faker                       faker           = new Faker();
    private             TextField                   shortcodeField;
    private             TextField                   nameField;
    private             TextField                   emailField;

    public FacultyPage() {
        super();
        addPageHeader();
        addFormattedText("Manage faculty profiles with their shortcode and assigned subjects.");
        addSection("Faculty Table", createFacultyTable());
        addSection("Faculty Form", createFacultyForm());
        loadMockData();
    }

    @Override
    protected void updateTexts() {}

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createFacultyTable() {
        TableColumn<FacultyData, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        idColumn.setCellFactory(tc -> {
            TableCell<FacultyData, String> cell = new TableCell<>() {
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

        TableColumn<FacultyData, String> shortcodeColumn = new TableColumn<>("Shortcode");
        shortcodeColumn.setCellValueFactory(data -> data.getValue().shortcodeProperty());

        TableColumn<FacultyData, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<FacultyData, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());

        tableView.getColumns().addAll(idColumn, shortcodeColumn, nameColumn, emailColumn);
        tableView.setItems(facultyDataList);
        tableView.setRowFactory(tv -> {
            TableRow<FacultyData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    FacultyData rowData = row.getItem();
                    shortcodeField.setText(rowData.getShortcode());
                    nameField.setText(rowData.getName());
                    emailField.setText(rowData.getEmail());
                }
            });
            return row;
        });

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createFacultyForm() {
        shortcodeField = new TextField();
        nameField      = new TextField();
        emailField     = new TextField();

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("Shortcode:"), shortcodeField, new Label("Name:"), nameField, new Label("Email:"), emailField, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            facultyDataList.add(new FacultyData(UUID.randomUUID()
                                                    .toString(), shortcodeField.getText(), nameField.getText(), emailField.getText()));
        });

        updateBtn.setOnAction(e -> {
            FacultyData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setShortcode(shortcodeField.getText());
                selected.setName(nameField.getText());
                selected.setEmail(emailField.getText());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            FacultyData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                facultyDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            facultyDataList.add(new FacultyData(UUID.randomUUID().toString(), faker.lorem()
                                                                                   .characters(5)
                                                                                   .toUpperCase(), faker.name()
                                                                                                        .fullName(), faker.internet()
                                                                                                                          .emailAddress()));
        }
    }
}
