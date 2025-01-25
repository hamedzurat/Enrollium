package enrollium.client.page.database;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import net.datafaker.Faker;

import java.util.UUID;

public class SubjectPage extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SUBJECT;
    private final TableView<SubjectData> tableView = new TableView<>();
    private final ObservableList<SubjectData> subjectDataList = FXCollections.observableArrayList();
    private final Faker faker = new Faker();
    private TextField nameField;
    private TextField codeNameField;
    private TextField creditsField;
    private ComboBox<String> typeDropdown;

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
        // Define table columns
        TableColumn<SubjectData, String> idColumn = new TableColumn<>("ID");
        TableColumn<SubjectData, String> nameColumn = new TableColumn<>("Name");
        TableColumn<SubjectData, String> codeNameColumn = new TableColumn<>("Code Name");
        TableColumn<SubjectData, String> creditsColumn = new TableColumn<>("Credits");
        TableColumn<SubjectData, String> typeColumn = new TableColumn<>("Type");

        // Bind columns to SubjectData properties
        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        codeNameColumn.setCellValueFactory(data -> data.getValue().codeNameProperty());
        creditsColumn.setCellValueFactory(data -> data.getValue().creditsProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());

        // Add columns to the table
        tableView.getColumns().addAll(idColumn, nameColumn, codeNameColumn, creditsColumn, typeColumn);
        tableView.setItems(subjectDataList);

        // Handle row clicks to populate the form
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

        VBox container = new VBox(10, tableView);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox createSubjectForm() {
        // Initialize form fields
        nameField = new TextField();
        codeNameField = new TextField();
        creditsField = new TextField();
        typeDropdown = new ComboBox<>(FXCollections.observableArrayList("THEORY", "LAB"));

        // Create action buttons using DatabaseUiUtils
        VBox form = new VBox(10,
                new Label("Name:"), nameField,
                new Label("Code Name:"), codeNameField,
                new Label("Credits:"), creditsField,
                new Label("Type:"), typeDropdown,
                DatabaseUiUtils.createActionButtons(
                        // Create Action
                        () -> subjectDataList.add(new SubjectData(
                                UUID.randomUUID().toString(),
                                nameField.getText(),
                                codeNameField.getText(),
                                creditsField.getText(),
                                typeDropdown.getValue())),
                        // Update Action
                        () -> {
                            SubjectData selected = tableView.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                selected.setName(nameField.getText());
                                selected.setCodeName(codeNameField.getText());
                                selected.setCredits(creditsField.getText());
                                selected.setType(typeDropdown.getValue());
                                tableView.refresh();
                            }
                        },
                        // Delete Action
                        () -> {
                            SubjectData selected = tableView.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                subjectDataList.remove(selected);
                            }
                        },
                        // Fill for Demo Action
                        this::loadMockData
                )
        );

        form.setPadding(new Insets(10));
        return form;
    }

    private void loadMockData() {
        subjectDataList.clear();
        for (int i = 0; i < 10; i++) {
            subjectDataList.add(new SubjectData(
                    UUID.randomUUID().toString(),
                    faker.educator().course(),
                    faker.educator().campus(),
                    String.valueOf(faker.number().numberBetween(1, 5)),
                    faker.options().option("THEORY", "LAB")));
        }
    }

    // SubjectData class
    public static class SubjectData {
        private final StringProperty id;
        private final StringProperty name;
        private final StringProperty codeName;
        private final StringProperty credits;
        private final StringProperty type;

        public SubjectData(String id, String name, String codeName, String credits, String type) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.codeName = new SimpleStringProperty(codeName);
            this.credits = new SimpleStringProperty(credits);
            this.type = new SimpleStringProperty(type);
        }

        public StringProperty idProperty() {
            return id;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty codeNameProperty() {
            return codeName;
        }

        public StringProperty creditsProperty() {
            return credits;
        }

        public StringProperty typeProperty() {
            return type;
        }

        public String getId() {
            return id.get();
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getCodeName() {
            return codeName.get();
        }

        public void setCodeName(String codeName) {
            this.codeName.set(codeName);
        }

        public String getCredits() {
            return credits.get();
        }

        public void setCredits(String credits) {
            this.credits.set(credits);
        }

        public String getType() {
            return type.get();
        }

        public void setType(String type) {
            this.type.set(type);
        }
    }
}
