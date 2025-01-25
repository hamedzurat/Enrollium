package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import net.datafaker.Faker;

import java.util.UUID;

public class TrimesterPage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.TRIMESTER;
    private final TableView<TrimesterData> tableView = new TableView<>();
    private final ObservableList<TrimesterData> trimesterDataList = FXCollections.observableArrayList();
    private final Faker faker = new Faker();
    private TextField codeField;
    private TextField yearField;
    private ComboBox<String> seasonDropdown;
    private ComboBox<String> statusDropdown;

    public TrimesterPage() {
        super();
        addPageHeader();
        addFormattedText("Manage academic trimesters with code, year, season, and status.");
        addSection("Trimester Table", createTrimesterTable());
        addSection("Trimester Form", createTrimesterForm());
        loadMockData();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createTrimesterTable() {
        TableColumn<TrimesterData, String> idColumn = new TableColumn<>("ID");
        TableColumn<TrimesterData, String> codeColumn = new TableColumn<>("Code");
        TableColumn<TrimesterData, String> yearColumn = new TableColumn<>("Year");
        TableColumn<TrimesterData, String> seasonColumn = new TableColumn<>("Season");
        TableColumn<TrimesterData, String> statusColumn = new TableColumn<>("Status");

        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        codeColumn.setCellValueFactory(data -> data.getValue().codeProperty());
        yearColumn.setCellValueFactory(data -> data.getValue().yearProperty());
        seasonColumn.setCellValueFactory(data -> data.getValue().seasonProperty());
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
        codeField = new TextField();
        yearField = new TextField();
        seasonDropdown = new ComboBox<>(FXCollections.observableArrayList("SPRING", "SUMMER", "FALL"));
        statusDropdown = new ComboBox<>(FXCollections.observableArrayList("UPCOMING", "COURSE_SELECTION", "SECTION_CREATION", "SECTION_SELECTION", "ONGOING", "COMPLETED"));

        VBox form = new VBox(10,
                new Label("Code:"), codeField,
                new Label("Year:"), yearField,
                new Label("Season:"), seasonDropdown,
                new Label("Status:"), statusDropdown
        );
        form.setPadding(new Insets(10));

        VBox actions = DatabaseUiUtils.createActionButtons(
                () -> {
                    // Create action
                    trimesterDataList.add(new TrimesterData(
                            UUID.randomUUID().toString(),
                            codeField.getText(),
                            yearField.getText(),
                            seasonDropdown.getValue(),
                            statusDropdown.getValue()
                    ));
                    clearForm();
                },
                () -> {
                    // Update action
                    TrimesterData selected = tableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        selected.setCode(codeField.getText());
                        selected.setYear(yearField.getText());
                        selected.setSeason(seasonDropdown.getValue());
                        selected.setStatus(statusDropdown.getValue());
                        tableView.refresh();
                        clearForm();
                    }
                },
                () -> {
                    // Delete action
                    TrimesterData selected = tableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        trimesterDataList.remove(selected);
                        clearForm();
                    }
                },
                this::loadMockData // Fill for demo action
        );

        form.getChildren().add(actions);
        return form;
    }

    private void clearForm() {
        codeField.clear();
        yearField.clear();
        seasonDropdown.setValue(null);
        statusDropdown.setValue(null);
    }

    private void loadMockData() {
        trimesterDataList.clear();
        for (int i = 0; i < 10; i++) {
            trimesterDataList.add(new TrimesterData(UUID.randomUUID().toString(),
                    faker.number().digits(3),
                    String.valueOf(faker.number().numberBetween(2003, 2025)),
                    faker.options().option("SPRING", "SUMMER", "FALL"),
                    faker.options().option("UPCOMING", "ONGOING", "COMPLETED")));
        }
    }
}
