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


public class SubjectPage extends BasePage {
    public static final TranslationKey                   NAME            = TranslationKey.SUBJECT;
    private final       TableView<SubjectData>           tableView       = new TableView<>();
    private final       ObservableList<SubjectData>      subjectDataList = FXCollections.observableArrayList();
    private final       Faker                            faker           = new Faker();
    private final       TableColumn<SubjectData, String> idColumn        = new TableColumn<>("ID");
    private final       TableColumn<SubjectData, String> nameColumn      = new TableColumn<>("Name");
    private final       TableColumn<SubjectData, String> codeNameColumn  = new TableColumn<>("Code Name");
    private final       TableColumn<SubjectData, String> creditsColumn   = new TableColumn<>("Credits");
    private final       TableColumn<SubjectData, String> typeColumn      = new TableColumn<>("Type");
    private             TextField                        nameField;
    private             TextField                        codeNameField;
    private             TextField                        creditsField;
    private             ComboBox<String>                 typeDropdown;

    public SubjectPage() {
        super();
        addPageHeader();
        addFormattedText("Manage academic subjects with their code name, credits, and type.");
        addSection("Subjects Table", createSubjectTable());
        addSection("Subject Form", createSubjectForm());
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

    private VBox createSubjectTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        codeNameColumn.setCellValueFactory(data -> data.getValue().codeNameProperty());
        creditsColumn.setCellValueFactory(data -> data.getValue().creditsProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());

        idColumn.setCellFactory(tc -> {
            TableCell<SubjectData, String> cell = new TableCell<>() {
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

        tableView.getColumns().addAll(idColumn, nameColumn, codeNameColumn, creditsColumn, typeColumn);
        tableView.setItems(subjectDataList);
        tableView.setRowFactory(tv -> {
            TableRow<SubjectData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    SubjectData rowData = row.getItem();
                    nameField.setText(rowData.getName());
                    codeNameField.setText(rowData.getCodeName());
                    creditsField.setText(rowData.getCredits());
                    typeDropdown.setValue(rowData.getType());
                }
            });
            return row;
        });

        Utils.styleCourseTableView(tableView);
        subjectDataList.addListener((InvalidationListener) change -> Utils.adjustTableHeight(tableView));

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createSubjectForm() {
        nameField     = new TextField();
        codeNameField = new TextField();
        creditsField  = new TextField();
        typeDropdown  = new ComboBox<>(FXCollections.observableArrayList("THEORY", "LAB"));

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("Name:"), nameField, new Label("Code Name:"), codeNameField, new Label("Credits:"), creditsField, new Label("Type:"), typeDropdown, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            subjectDataList.add(new SubjectData(UUID.randomUUID()
                                                    .toString(), nameField.getText(), codeNameField.getText(), creditsField.getText(), typeDropdown.getValue()));
        });

        updateBtn.setOnAction(e -> {
            SubjectData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setName(nameField.getText());
                selected.setCodeName(codeNameField.getText());
                selected.setCredits(creditsField.getText());
                selected.setType(typeDropdown.getValue());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            SubjectData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                subjectDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            subjectDataList.add(new SubjectData(UUID.randomUUID().toString(), faker.educator()
                                                                                   .course(), faker.educator()
                                                                                                   .campus(), String.valueOf(faker.number()
                                                                                                                                  .numberBetween(1, 5)), faker.options()
                                                                                                                                                              .option("THEORY", "LAB")));
        }
    }
}
