package enrollium.client.page.general;

import enrollium.client.page.OutlinePage;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SectionTableView extends OutlinePage {

    public static final String NAME = "Section Table View";

    private final ObservableList<Section> sections = getSampleData();

    public SectionTableView() {
        super();

        // Add page header
        addPageHeader();

        // Add table section
        addSection("Section Details", createTableView());

        // Add actions section
        addSection("Actions", createActionsBox());
    }

    @Override
    public String getName() {
        return NAME;
    }

    private VBox createTableView() {
        VBox root = new VBox(10);

        TableView<Section> table = new TableView<>();

        // Define Columns
        TableColumn<Section, String> nameColumn = new TableColumn<>("Section Name");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Section, String> idColumn = new TableColumn<>("Identifier");
        idColumn.setCellValueFactory(data -> data.getValue().sectionProperty());

        TableColumn<Section, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(data -> data.getValue().subjectProperty());

        TableColumn<Section, String> trimesterColumn = new TableColumn<>("Trimester");
        trimesterColumn.setCellValueFactory(data -> data.getValue().trimesterProperty());

        TableColumn<Section, Integer> maxCapacityColumn = new TableColumn<>("Max Capacity");
        maxCapacityColumn.setCellValueFactory(data -> data.getValue().maxCapacityProperty().asObject());

        TableColumn<Section, Integer> currentCapacityColumn = new TableColumn<>("Current Capacity");
        currentCapacityColumn.setCellValueFactory(data -> data.getValue().currentCapacityProperty().asObject());

        // Add columns to the table
        table.getColumns().addAll(nameColumn, idColumn, subjectColumn, trimesterColumn, maxCapacityColumn, currentCapacityColumn);

        // Set items to the table
        table.setItems(sections);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        root.getChildren().addAll(new Label("Sections"), table);

        // Store table for actions
        root.setUserData(table);

        return root;
    }

    private HBox createActionsBox() {
        HBox actionsBox = new HBox(10);

        // Add Edit Button
        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> {
            TableView<Section> table = (TableView<Section>) actionsBox.getParent().lookup(".table-view");
            Section selectedSection = table.getSelectionModel().getSelectedItem();
            if (selectedSection != null) {
                openEditPopup(selectedSection, table);
            } else {
                showAlert("Error", "Please select a section to edit.");
            }
        });

        // Add Delete Button
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            TableView<Section> table = (TableView<Section>) actionsBox.getParent().lookup(".table-view");
            Section selectedSection = table.getSelectionModel().getSelectedItem();
            if (selectedSection != null) {
                sections.remove(selectedSection);
            } else {
                showAlert("Error", "Please select a section to delete.");
            }
        });

        actionsBox.getChildren().addAll(editButton, deleteButton);
        return actionsBox;
    }

    private void openEditPopup(Section section, TableView<Section> table) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Edit Section");

        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(10));

        TextField nameField = new TextField(section.getName());
        nameField.setPromptText("Section Name");

        TextField idField = new TextField(section.getSection());
        idField.setPromptText("Identifier");

        TextField subjectField = new TextField(section.getSubject());
        subjectField.setPromptText("Subject");

        TextField trimesterField = new TextField(section.getTrimester());
        trimesterField.setPromptText("Trimester");

        TextField maxCapacityField = new TextField(String.valueOf(section.getMaxCapacity()));
        maxCapacityField.setPromptText("Max Capacity");

        TextField currentCapacityField = new TextField(String.valueOf(section.getCurrentCapacity()));
        currentCapacityField.setPromptText("Current Capacity");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            section.setName(nameField.getText());
            section.setSection(idField.getText());
            section.setSubject(subjectField.getText());
            section.setTrimester(trimesterField.getText());
            section.setMaxCapacity(Integer.parseInt(maxCapacityField.getText()));
            section.setCurrentCapacity(Integer.parseInt(currentCapacityField.getText()));

            table.refresh();
            popupStage.close();
        });

        layout.getChildren().addAll(new Label("Edit Section Details"), nameField, idField, subjectField, trimesterField, maxCapacityField, currentCapacityField, saveButton);

        popupStage.setScene(new Scene(layout, 300, 400));
        popupStage.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ObservableList<Section> getSampleData() {
        ObservableList<Section> data = FXCollections.observableArrayList();

        // Add sample data for testing
        Section section1 = new Section("Math A", "01A", "Math", "Fall 2025", 30, 25);
        Section section2 = new Section("Science B", "02B", "Science", "Fall 2025", 30, 20);

        data.addAll(section1, section2);

        return data;
    }

    // Inner class representing a Section entity
    public static class Section {
        private final SimpleStringProperty name;
        private final SimpleStringProperty section;
        private final SimpleStringProperty subject;
        private final SimpleStringProperty trimester;
        private final SimpleIntegerProperty maxCapacity;
        private final SimpleIntegerProperty currentCapacity;

        public Section(String name, String section, String subject, String trimester, int maxCapacity, int currentCapacity) {
            this.name = new SimpleStringProperty(name);
            this.section = new SimpleStringProperty(section);
            this.subject = new SimpleStringProperty(subject);
            this.trimester = new SimpleStringProperty(trimester);
            this.maxCapacity = new SimpleIntegerProperty(maxCapacity);
            this.currentCapacity = new SimpleIntegerProperty(currentCapacity);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getSection() {
            return section.get();
        }

        public void setSection(String section) {
            this.section.set(section);
        }

        public String getSubject() {
            return subject.get();
        }

        public void setSubject(String subject) {
            this.subject.set(subject);
        }

        public String getTrimester() {
            return trimester.get();
        }

        public void setTrimester(String trimester) {
            this.trimester.set(trimester);
        }

        public int getMaxCapacity() {
            return maxCapacity.get();
        }

        public void setMaxCapacity(int maxCapacity) {
            this.maxCapacity.set(maxCapacity);
        }

        public int getCurrentCapacity() {
            return currentCapacity.get();
        }

        public void setCurrentCapacity(int currentCapacity) {
            this.currentCapacity.set(currentCapacity);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public SimpleStringProperty sectionProperty() {
            return section;
        }

        public SimpleStringProperty subjectProperty() {
            return subject;
        }

        public SimpleStringProperty trimesterProperty() {
            return trimester;
        }

        public SimpleIntegerProperty maxCapacityProperty() {
            return maxCapacity;
        }

        public SimpleIntegerProperty currentCapacityProperty() {
            return currentCapacity;
        }
    }
}
