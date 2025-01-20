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


public class SectionPage extends BasePage {
    public static final TranslationKey                   NAME            = TranslationKey.SECTION;
    private final       TableView<SectionData>           tableView       = new TableView<>();
    private final       ObservableList<SectionData>      sectionDataList = FXCollections.observableArrayList();
    private final       Faker                            faker           = new Faker();
    private final       TableColumn<SectionData, String> idColumn        = new TableColumn<>("ID");
    private final       TableColumn<SectionData, String> nameColumn      = new TableColumn<>("Name");
    private final       TableColumn<SectionData, String> subjectColumn   = new TableColumn<>("Subject");
    private final       TableColumn<SectionData, String> trimesterColumn = new TableColumn<>("Trimester");
    private final       TableColumn<SectionData, String> capacityColumn  = new TableColumn<>("Capacity");
    private             TextField                        nameField;
    private             ComboBox<String>                 subjectDropdown;
    private             ComboBox<String>                 trimesterDropdown;
    private             TextField                        maxCapacityField;

    public SectionPage() {
        super();
        addPageHeader();
        addFormattedText("Manage course sections with their subject, trimester, and capacities.");
        addSection("Sections Table", createSectionTable());
        addSection("Section Form", createSectionForm());
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

    private VBox createSectionTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        subjectColumn.setCellValueFactory(data -> data.getValue().subjectProperty());
        trimesterColumn.setCellValueFactory(data -> data.getValue().trimesterProperty());
        capacityColumn.setCellValueFactory(data -> data.getValue().capacityProperty());

        idColumn.setCellFactory(tc -> {
            TableCell<SectionData, String> cell = new TableCell<>() {
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

        tableView.getColumns().addAll(idColumn, nameColumn, subjectColumn, trimesterColumn, capacityColumn);
        tableView.setItems(sectionDataList);
        tableView.setRowFactory(tv -> {
            TableRow<SectionData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    SectionData rowData = row.getItem();
                    nameField.setText(rowData.getName());
                    subjectDropdown.setValue(rowData.getSubject());
                    trimesterDropdown.setValue(rowData.getTrimester());
                    maxCapacityField.setText(rowData.getMaxCapacity());
                }
            });
            return row;
        });

        Utils.styleCourseTableView(tableView);
        sectionDataList.addListener((InvalidationListener) change -> Utils.adjustTableHeight(tableView));

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createSectionForm() {
        nameField         = new TextField();
        subjectDropdown   = new ComboBox<>(FXCollections.observableArrayList("Math", "Physics", "Chemistry"));
        trimesterDropdown = new ComboBox<>(FXCollections.observableArrayList("Spring 2024", "Summer 2024", "Fall 2024"));
        maxCapacityField  = new TextField();

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("Name:"), nameField, new Label("Subject:"), subjectDropdown, new Label("Trimester:"), trimesterDropdown, new Label("Max Capacity:"), maxCapacityField, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            sectionDataList.add(new SectionData(UUID.randomUUID()
                                                    .toString(), nameField.getText(), subjectDropdown.getValue(), trimesterDropdown.getValue(), maxCapacityField.getText()));
        });

        updateBtn.setOnAction(e -> {
            SectionData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setName(nameField.getText());
                selected.setSubject(subjectDropdown.getValue());
                selected.setTrimester(trimesterDropdown.getValue());
                selected.setMaxCapacity(maxCapacityField.getText());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            SectionData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                sectionDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            sectionDataList.add(new SectionData(UUID.randomUUID().toString(), faker.educator()
                                                                                   .course(), faker.educator()
                                                                                                   .campus(), "Spring 2024", String.valueOf(faker.number()
                                                                                                                                                 .numberBetween(20, 100))));
        }
    }
}
