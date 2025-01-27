package enrollium.client.page.students;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class History extends BasePage {

    public static final TranslationKey NAME = TranslationKey.History;

    // Table components
    private final TableView<HistoryData> historyTable = new TableView<>();
    private final ObservableList<HistoryData> historyDataList = FXCollections.observableArrayList();

    // Table columns
    private final TableColumn<HistoryData, String> courseNameColumn = new TableColumn<>("Course Name");
    private final TableColumn<HistoryData, String> statusColumn = new TableColumn<>("Status");
    private final TableColumn<HistoryData, String> gradeColumn = new TableColumn<>("Grade");

    public History() {
        super();

        addPageHeader(); // Add a header to the page

        // Create and configure the History Table Section
        VBox historyTableView = createHistoryTable();

        // Add the History Table to the page
        addSection("Student Course History", historyTableView);

        // Load historical data
        loadHistoryData();
    }

    private VBox createHistoryTable() {
        // Configure columns
        courseNameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        gradeColumn.setCellValueFactory(data -> data.getValue().gradeProperty());

        // Add columns to the table
        historyTable.getColumns().addAll(courseNameColumn, statusColumn, gradeColumn);

        // Add data to the table
        historyTable.setItems(historyDataList);

        // Set table properties
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add the table to a container
        VBox container = new VBox(10, historyTable);
        container.setPadding(new Insets(20));
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        return container;
    }

    private void loadHistoryData() {
        // Sample data for history
        historyDataList.add(new HistoryData("English–I", "Completed", "3.33"));
        historyDataList.add(new HistoryData("Discrete Mathematics", "Completed", "3.00"));
        historyDataList.add(new HistoryData("Structured Programming Language", "Completed", "3.33"));
        historyDataList.add(new HistoryData("Fundamental Calculus", "Completed", "2.67"));
        historyDataList.add(new HistoryData("Calculus and Linear Algebra", "Registered", "-"));
        historyDataList.add(new HistoryData("Digital Logic Design", "Registered", "-"));
        historyDataList.add(new HistoryData("Object Oriented Programming", "Registered", "-"));
        historyDataList.add(new HistoryData("History of the Emergence of Bangladesh", "Withdrawn", "-"));
        historyDataList.add(new HistoryData("English–II", "Withdrawn", "-"));
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}

// Helper class for table data
class HistoryData {
    private final StringProperty courseName;
    private final StringProperty status;
    private final StringProperty grade;

    public HistoryData(String courseName, String status, String grade) {
        this.courseName = new SimpleStringProperty(courseName);
        this.status = new SimpleStringProperty(status);
        this.grade = new SimpleStringProperty(grade);
    }

    public StringProperty courseNameProperty() {
        return courseName;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty gradeProperty() {
        return grade;
    }
}
