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


public class CoursePage extends OutlinePage {
    public static final TranslationKey             NAME           = TranslationKey.COURSE;
    private final       TableView<CourseData>      tableView      = new TableView<>();
    private final       ObservableList<CourseData> courseDataList = FXCollections.observableArrayList();
    private final       ObservableList<String>     studentList    = FXCollections.observableArrayList();
    private final       Faker                      faker          = new Faker();
    private             TextField                  statusField;
    private             ComboBox<String>           studentDropdown;

    public CoursePage() {
        super();
        addPageHeader();
        addFormattedText("Manage course registrations with status, grade, and section details.");
        addSection("Courses Table", createCourseTable());
        addSection("Course Form", createCourseForm());
        loadMockData();
    }

    @Override
    protected void updateTexts() {
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createCourseTable() {
        TableColumn<CourseData, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        idColumn.setCellFactory(tc -> {
            TableCell<CourseData, String> cell = new TableCell<>() {
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

        TableColumn<CourseData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        TableColumn<CourseData, String> studentColumn = new TableColumn<>("Student");
        studentColumn.setCellValueFactory(data -> data.getValue().studentProperty());

        tableView.getColumns().addAll(idColumn, statusColumn, studentColumn);
        tableView.setItems(courseDataList);
        tableView.setRowFactory(tv -> {
            TableRow<CourseData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    CourseData rowData = row.getItem();
                    statusField.setText(rowData.getStatus());
                    studentDropdown.setValue(rowData.getStudent());
                }
            });
            return row;
        });

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createCourseForm() {
        statusField     = new TextField();
        studentDropdown = new ComboBox<>(studentList);
        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("Status:"), statusField, new Label("Student:"), studentDropdown, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            courseDataList.add(new CourseData(UUID.randomUUID()
                                                  .toString(), statusField.getText(), studentDropdown.getValue()));
        });

        updateBtn.setOnAction(e -> {
            CourseData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setStatus(statusField.getText());
                selected.setStudent(studentDropdown.getValue());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            CourseData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                courseDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            String studentName = faker.name().fullName();
            studentList.add(studentName);
            courseDataList.add(new CourseData(UUID.randomUUID().toString(), faker.educator().course(), studentName));
        }
    }
}

