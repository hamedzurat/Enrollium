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


public class PrerequisitePage extends BasePage {
    public static final TranslationKey                        NAME                 = TranslationKey.PREREQUISITE;
    private final       TableView<PrerequisiteData>           tableView            = new TableView<>();
    private final       ObservableList<PrerequisiteData>      prerequisiteDataList = FXCollections.observableArrayList();
    private final       ObservableList<String>                subjectList          = FXCollections.observableArrayList("Math", "Physics", "Chemistry", "Biology", "Computer Science");
    private final       Faker                                 faker                = new Faker();
    private final       TableColumn<PrerequisiteData, String> idColumn             = new TableColumn<>("ID");
    private final       TableColumn<PrerequisiteData, String> subjectColumn        = new TableColumn<>("Subject");
    private final       TableColumn<PrerequisiteData, String> prerequisiteColumn   = new TableColumn<>("Prerequisite");
    private final       TableColumn<PrerequisiteData, String> minGradeColumn       = new TableColumn<>("Minimum Grade");
    private             ComboBox<String>                      subjectDropdown;
    private             ComboBox<String>                      prerequisiteDropdown;
    private             TextField                             minGradeField;

    public PrerequisitePage() {
        super();
        addPageHeader();
        addFormattedText("Manage subject prerequisites with minimum grade requirements.");
        addSection("Prerequisites Table", createPrerequisiteTable());
        addSection("Prerequisite Form", createPrerequisiteForm());
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

    private VBox createPrerequisiteTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        subjectColumn.setCellValueFactory(data -> data.getValue().subjectProperty());
        prerequisiteColumn.setCellValueFactory(data -> data.getValue().prerequisiteProperty());
        minGradeColumn.setCellValueFactory(data -> data.getValue().minimumGradeProperty());

        idColumn.setCellFactory(tc -> {
            TableCell<PrerequisiteData, String> cell = new TableCell<>() {
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

        tableView.getColumns().addAll(idColumn, subjectColumn, prerequisiteColumn, minGradeColumn);
        tableView.setItems(prerequisiteDataList);
        tableView.setRowFactory(tv -> {
            TableRow<PrerequisiteData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    PrerequisiteData rowData = row.getItem();
                    subjectDropdown.setValue(rowData.getSubject());
                    prerequisiteDropdown.setValue(rowData.getPrerequisite());
                    minGradeField.setText(rowData.getMinimumGrade());
                }
            });
            return row;
        });

        DatabaseUiUtils.styleCourseTableView(tableView);
        prerequisiteDataList.addListener((InvalidationListener) change -> DatabaseUiUtils.adjustTableHeight(tableView));

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createPrerequisiteForm() {
        subjectDropdown      = new ComboBox<>(subjectList);
        prerequisiteDropdown = new ComboBox<>(subjectList);
        minGradeField        = new TextField();

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("Subject:"), subjectDropdown, new Label("Prerequisite Subject:"), prerequisiteDropdown, new Label("Minimum Grade (0.0 - 4.0):"), minGradeField, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            prerequisiteDataList.add(new PrerequisiteData(UUID.randomUUID()
                                                              .toString(), subjectDropdown.getValue(), prerequisiteDropdown.getValue(), minGradeField.getText()));
        });

        updateBtn.setOnAction(e -> {
            PrerequisiteData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setSubject(subjectDropdown.getValue());
                selected.setPrerequisite(prerequisiteDropdown.getValue());
                selected.setMinimumGrade(minGradeField.getText());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            PrerequisiteData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                prerequisiteDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            prerequisiteDataList.add(new PrerequisiteData(UUID.randomUUID().toString(), faker.educator()
                                                                                             .course(), faker.educator()
                                                                                                             .course(), String.format("%.2f", faker.number()
                                                                                                                                                   .randomDouble(1, 0, 4))));
        }
    }
}
