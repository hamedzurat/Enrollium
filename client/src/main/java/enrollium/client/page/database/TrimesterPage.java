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


public class TrimesterPage extends OutlinePage {
    public static final TranslationKey                NAME              = TranslationKey.TRIMESTER;
    private final       TableView<TrimesterData>      tableView         = new TableView<>();
    private final       ObservableList<TrimesterData> trimesterDataList = FXCollections.observableArrayList();
    private final       Faker                         faker             = new Faker();
    private             TextField                     codeField;
    private             TextField                     yearField;
    private             ComboBox<String>              seasonDropdown;
    private             ComboBox<String>              statusDropdown;

    public TrimesterPage() {
        super();
        addPageHeader();
        addFormattedText("Manage academic trimesters with code, year, season, and status.");
        addSection("Trimester Table", createTrimesterTable());
        addSection("Trimester Form", createTrimesterForm());
        loadMockData();
    }

    @Override
    protected void updateTexts() {}

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createTrimesterTable() {
        TableColumn<TrimesterData, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        idColumn.setCellFactory(tc -> {
            TableCell<TrimesterData, String> cell = new TableCell<>() {
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

        TableColumn<TrimesterData, String> codeColumn = new TableColumn<>("Code");
        codeColumn.setCellValueFactory(data -> data.getValue().codeProperty());

        TableColumn<TrimesterData, String> yearColumn = new TableColumn<>("Year");
        yearColumn.setCellValueFactory(data -> data.getValue().yearProperty());

        TableColumn<TrimesterData, String> seasonColumn = new TableColumn<>("Season");
        seasonColumn.setCellValueFactory(data -> data.getValue().seasonProperty());

        TableColumn<TrimesterData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        tableView.getColumns().addAll(idColumn, codeColumn, yearColumn, seasonColumn, statusColumn);
        tableView.setItems(trimesterDataList);
        tableView.setRowFactory(tv -> {
            TableRow<TrimesterData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    TrimesterData rowData = row.getItem();
                    codeField.setText(rowData.getCode());
                    yearField.setText(rowData.getYear());
                    seasonDropdown.setValue(rowData.getSeason());
                    statusDropdown.setValue(rowData.getStatus());
                }
            });
            return row;
        });

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createTrimesterForm() {
        codeField      = new TextField();
        yearField      = new TextField();
        seasonDropdown = new ComboBox<>(FXCollections.observableArrayList("SPRING", "SUMMER", "FALL"));
        statusDropdown = new ComboBox<>(FXCollections.observableArrayList("UPCOMING", "COURSE_SELECTION", "SECTION_CREATION", "SECTION_SELECTION", "ONGOING", "COMPLETED"));

        Button createBtn = new Button("Create");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        VBox form = new VBox(10, new Label("Code:"), codeField, new Label("Year:"), yearField, new Label("Season:"), seasonDropdown, new Label("Status:"), statusDropdown, actions);
        form.setPadding(new Insets(10));

        createBtn.setOnAction(e -> {
            trimesterDataList.add(new TrimesterData(UUID.randomUUID()
                                                        .toString(), codeField.getText(), yearField.getText(), seasonDropdown.getValue(), statusDropdown.getValue()));
        });

        updateBtn.setOnAction(e -> {
            TrimesterData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setCode(codeField.getText());
                selected.setYear(yearField.getText());
                selected.setSeason(seasonDropdown.getValue());
                selected.setStatus(statusDropdown.getValue());
                tableView.refresh();
            }
        });

        deleteBtn.setOnAction(e -> {
            TrimesterData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                trimesterDataList.remove(selected);
            }
        });

        return form;
    }

    private void loadMockData() {
        for (int i = 0; i < 10; i++) {
            trimesterDataList.add(new TrimesterData(UUID.randomUUID().toString(), faker.number()
                                                                                       .digits(3), String.valueOf(faker.number()
                                                                                                                       .numberBetween(2003, 2025)), faker.options()
                                                                                                                                                         .option("SPRING", "SUMMER", "FALL"), faker.options()
                                                                                                                                                                                                   .option("UPCOMING", "ONGOING", "COMPLETED")));
        }
    }
}
