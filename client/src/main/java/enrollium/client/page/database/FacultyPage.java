package enrollium.client.page.database;

import atlantafx.base.theme.Styles;
import enrollium.client.page.BasePage;
import enrollium.client.page.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.datafaker.Faker;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.UUID;

public class FacultyPage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.FACULTY;

    private final TableView<FacultyData> tableView = new TableView<>();
    private final ObservableList<FacultyData> facultyDataList = FXCollections.observableArrayList();
    private final Faker faker = new Faker();
    private final TableColumn<FacultyData, String> idColumn = new TableColumn<>("ID");
    private final TableColumn<FacultyData, String> shortcodeColumn = new TableColumn<>("Shortcode");
    private final TableColumn<FacultyData, String> nameColumn = new TableColumn<>("Name");
    private final TableColumn<FacultyData, String> emailColumn = new TableColumn<>("Email");
    private TextField shortcodeField;
    private TextField nameField;
    private TextField emailField;

    public FacultyPage() {
        super();
        addPageHeader();
        addFormattedText("Manage faculty profiles with their shortcode and assigned subjects.");
        addSection("Faculty Table", createFacultyTable());
        addSection("Faculty Form", createFacultyForm());
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

    private VBox createFacultyTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        shortcodeColumn.setCellValueFactory(data -> data.getValue().shortcodeProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());

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

        DatabaseUiUtils.styleCourseTableView(tableView);
        facultyDataList.addListener((InvalidationListener) change -> DatabaseUiUtils.adjustTableHeight(tableView));

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createFacultyForm() {
        shortcodeField = new TextField();
        nameField = new TextField();
        emailField = new TextField();

        VBox actions = DatabaseUiUtils.createActionButtons(
                this::createFaculty,
                this::updateFaculty,
                this::deleteFaculty,
                this::fillDemoData
        );

        VBox form = new VBox(10,
                new Label("Shortcode:"), shortcodeField,
                new Label("Name:"), nameField,
                new Label("Email:"), emailField,
                actions
        );
        form.setPadding(new Insets(10));

        return form;
    }

    private void createFaculty() {
        if (shortcodeField.getText().isEmpty() || nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showNotification("All fields are required", NotificationType.WARNING);
            return;
        }

        String id = UUID.randomUUID().toString();
        FacultyData faculty = new FacultyData(id, shortcodeField.getText(), nameField.getText(), emailField.getText());
        facultyDataList.add(faculty);
        showNotification("Faculty created successfully", NotificationType.SUCCESS);
        clearForm();
    }

    private void updateFaculty() {
        FacultyData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Please select a faculty to update", NotificationType.WARNING);
            return;
        }

        if (shortcodeField.getText().isEmpty() || nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showNotification("All fields are required", NotificationType.WARNING);
            return;
        }

        selected.setShortcode(shortcodeField.getText());
        selected.setName(nameField.getText());
        selected.setEmail(emailField.getText());
        tableView.refresh();
        showNotification("Faculty updated successfully", NotificationType.SUCCESS);
        clearForm();
    }

    private void deleteFaculty() {
        FacultyData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Please select a faculty to delete", NotificationType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Faculty");
        alert.setHeaderText("Delete Faculty");
        alert.setContentText("Are you sure you want to delete this faculty?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                facultyDataList.remove(selected);
                showNotification("Faculty deleted successfully", NotificationType.SUCCESS);
                clearForm();
            }
        });
    }

    private void clearForm() {
        shortcodeField.clear();
        nameField.clear();
        emailField.clear();
        tableView.getSelectionModel().clearSelection();
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

    private void fillDemoData() {
        Platform.runLater(() -> {
            shortcodeField.setText("DEMO");
            nameField.setText("Demo User");
            emailField.setText("demo@example.com");
        });
    }
}
