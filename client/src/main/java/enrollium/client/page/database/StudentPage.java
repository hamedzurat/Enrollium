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


public class StudentPage extends BasePage {
    public static final TranslationKey                   NAME               = TranslationKey.STUDENT;
    private final       TableView<StudentData>           tableView          = new TableView<>();
    private final       ObservableList<StudentData>      studentDataList    = FXCollections.observableArrayList();
    private final       Faker                            faker              = new Faker();
    private final       TableColumn<StudentData, String> idColumn           = new TableColumn<>("ID");
    private final       TableColumn<StudentData, String> universityIdColumn = new TableColumn<>("University ID");
    private final       TableColumn<StudentData, String> nameColumn         = new TableColumn<>("Name");
    private final       TableColumn<StudentData, String> emailColumn        = new TableColumn<>("Email");
    private             TextField                        universityIdField;
    private             TextField                        nameField;
    private             TextField                        emailField;

    public StudentPage() {
        super();
        addPageHeader();
        addFormattedText("Manage student profiles with university IDs and personal information.");
        addSection("Students Table", createStudentTable());
        addSection("Student Form", createStudentForm());
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

    private VBox createStudentTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        universityIdColumn.setCellValueFactory(data -> data.getValue().universityIdProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());

        idColumn.setCellFactory(tc -> {
            TableCell<StudentData, String> cell = new TableCell<>() {
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

        tableView.getColumns().addAll(idColumn, universityIdColumn, nameColumn, emailColumn);
        tableView.setItems(studentDataList);
        tableView.setRowFactory(tv -> {
            TableRow<StudentData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    StudentData rowData = row.getItem();
                    universityIdField.setText(rowData.getUniversityId());
                    nameField.setText(rowData.getName());
                    emailField.setText(rowData.getEmail());
                }
            });
            return row;
        });

        DatabaseUiUtils.styleCourseTableView(tableView);
        studentDataList.addListener((InvalidationListener) change -> DatabaseUiUtils.adjustTableHeight(tableView));

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createStudentForm() {
        universityIdField = new TextField();
        nameField         = new TextField();
        emailField        = new TextField();

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("University ID:"), universityIdField, new Label("Name:"), nameField, new Label("Email:"), emailField, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            studentDataList.add(new StudentData(UUID.randomUUID()
                                                    .toString(), universityIdField.getText(), nameField.getText(), emailField.getText()));
        });

        updateBtn.setOnAction(e -> {
            StudentData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setUniversityId(universityIdField.getText());
                selected.setName(nameField.getText());
                selected.setEmail(emailField.getText());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            StudentData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                studentDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            studentDataList.add(new StudentData(UUID.randomUUID().toString(), String.valueOf(faker.number()
                                                                                                  .randomNumber(6, true)), faker.name()
                                                                                                                                .fullName(), faker.internet()
                                                                                                                                                  .emailAddress()));
        }
    }
}

