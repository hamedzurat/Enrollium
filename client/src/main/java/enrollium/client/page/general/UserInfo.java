package enrollium.client.page.general;

import enrollium.client.page.OutlinePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class UserInfo extends OutlinePage {
    public static final TranslationKey NAME             = TranslationKey.USERPAGE;
    private final       String         studentName      = "Mr X";
    private final       String         studentID        = "123456";
    private final       String         department       = "Computer Science";
    private final       String         email            = "mrx@gmail.com";
    private final       String         contactNumber    = "01123456789";
    private final       double         cgpa             = 3.85;
    private final       int            runningCourses   = 5;
    private final       int            completedCredits = 90;
    private final       int            maxCreditLimit   = 120;

    public UserInfo() {
        super();

        // Add page header
        addPageHeader();

        // Add sections
        addSection("Personal Information", createPersonalInfoContent());
        addSection("Academic Information", createAcademicInfoContent());
        addSection("Running Courses", createRunningCoursesContent());
    }

    @Override
    protected void updateTexts() {
        // Add dynamic text updates if necessary
    }

    private VBox createPersonalInfoContent() {
        VBox content = new VBox(10);
        content.getChildren().addAll(
                createLabelRow("Name:", studentName),
                createLabelRow("Student ID:", studentID),
                createLabelRow("Department:", department),
                createLabelRow("Email:", email),
                createLabelRow("Contact Number:", contactNumber)
        );
        return content;
    }

    private VBox createAcademicInfoContent() {
        VBox content = new VBox(10);
        content.getChildren().addAll(
                createLabelRow("CGPA:", String.valueOf(cgpa)),
                createLabelRow("Running Courses:", String.valueOf(runningCourses)),
                createLabelRow("Completed Credits:", String.valueOf(completedCredits)),
                createLabelRow("Max Credit Limit:", String.valueOf(maxCreditLimit))
        );
        return content;
    }

    private ScrollPane createRunningCoursesContent() {
        TableView<Course> table = createRunningCoursesTable();

        // Wrap the table in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300); // Adjust height as needed

        return scrollPane;
    }

    private TableView<Course> createRunningCoursesTable() {
        TableView<Course> table = new TableView<>();

        // Define Columns
        TableColumn<Course, String> courseCodeColumn = new TableColumn<>("Course Code");
        courseCodeColumn.setCellValueFactory(data -> data.getValue().courseCodeProperty());

        TableColumn<Course, String> courseNameColumn = new TableColumn<>("Course Name");
        courseNameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());

        TableColumn<Course, String> instructorColumn = new TableColumn<>("Instructor");
        instructorColumn.setCellValueFactory(data -> data.getValue().instructorProperty());

        TableColumn<Course, Integer> creditColumn = new TableColumn<>("Credits");
        creditColumn.setCellValueFactory(data -> data.getValue().creditsProperty().asObject());

        // Add Columns to Table
        table.getColumns().addAll(courseCodeColumn, courseNameColumn, instructorColumn, creditColumn);

        // Populate Table Data
        table.setItems(getRunningCoursesData());

        // Set table properties
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Fit columns to the width

        return table;
    }

    private ObservableList<Course> getRunningCoursesData() {
        ObservableList<Course> courses = FXCollections.observableArrayList();

        for (int i = 1; i <= runningCourses; i++) {
            courses.add(new Course("CS10" + i, "Course Name " + i, "Instructor " + i, 3));
        }

        return courses;
    }

    private HBox createLabelRow(String label, String value) {
        HBox row = new HBox(10);
        row.getChildren().addAll(new Text(label), new Text(value));
        return row;
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    // Inner Class to Represent Courses
    public static class Course {
        private final javafx.beans.property.SimpleStringProperty  courseCode;
        private final javafx.beans.property.SimpleStringProperty  courseName;
        private final javafx.beans.property.SimpleStringProperty  instructor;
        private final javafx.beans.property.SimpleIntegerProperty credits;

        public Course(String courseCode, String courseName, String instructor, int credits) {
            this.courseCode = new javafx.beans.property.SimpleStringProperty(courseCode);
            this.courseName = new javafx.beans.property.SimpleStringProperty(courseName);
            this.instructor = new javafx.beans.property.SimpleStringProperty(instructor);
            this.credits    = new javafx.beans.property.SimpleIntegerProperty(credits);
        }

        public javafx.beans.property.SimpleStringProperty courseCodeProperty() {
            return courseCode;
        }

        public javafx.beans.property.SimpleStringProperty courseNameProperty() {
            return courseName;
        }

        public javafx.beans.property.SimpleStringProperty instructorProperty() {
            return instructor;
        }

        public javafx.beans.property.SimpleIntegerProperty creditsProperty() {
            return credits;
        }
    }
}
