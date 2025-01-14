package enrollium.client.page.general;

import enrollium.client.page.Page;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Random;

public class Advising implements Page {

    private final HashMap<String, String> courseSchedule = new HashMap<>();
    private String selectedCourseCode = null;
    private String selectedSection = null;

    private GridPane planner;
    private Button takeCourseButton;

    public VBox createAdvisingUI() {
        populateCourseSchedule();

        // Root Layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        // Timetable Header
        Text timetableHeader = new Text("Course Timetable");
        timetableHeader.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 30));

        // Timetable Grid
        planner = new GridPane();
        planner.setHgap(10);
        planner.setVgap(10);
        planner.setPadding(new Insets(10));

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Saturday"};
        String[] timeSlots = {"8:30-9:50", "9:51-11:10", "11:11-12:30", "12:31-1:50", "1:51-3:10", "3:11-4:30"};

        configureGrid(days, timeSlots);
        populateTimetable(days, timeSlots);

        // Feedback Section
        VBox feedbackSection = new VBox(10);
        feedbackSection.setAlignment(Pos.CENTER);
        takeCourseButton = new Button("Take Course");
        feedbackSection.getChildren().addAll(takeCourseButton);

        // Handle "Take Course" button action
        takeCourseButton.setOnAction(event -> {
            if (selectedCourseCode != null && selectedSection != null) {
                if (courseSchedule.containsKey(selectedCourseCode)) {
                    String selectedCourseName = courseSchedule.get(selectedCourseCode);
                    String message = "Successfully registered for Section " + selectedSection + " of " + selectedCourseCode + " (" + selectedCourseName + ").";
                    showPopup("Success", message);
                } else {
                    String message = "Selected course does not exist in the schedule.";
                    showPopup("Error", message);
                }
            } else {
                String message = "Please select a course and section before registering.";
                showPopup("Error", message);
            }
        });

        // Assemble root layout
        root.getChildren().addAll(timetableHeader, planner, feedbackSection);
        return root;
    }

    private void configureGrid(String[] days, String[] timeSlots) {
        // Add column constraints
        for (int i = 0; i <= days.length; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / (days.length + 1));
            planner.getColumnConstraints().add(columnConstraints);
        }

        // Add row constraints
        for (int i = 0; i <= timeSlots.length; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / (timeSlots.length + 1));
            planner.getRowConstraints().add(rowConstraints);
        }

        // Add day headers
        for (int i = 0; i < days.length; i++) {
            Text dayLabel = new Text(days[i]);
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            GridPane.setValignment(dayLabel, VPos.CENTER);
            planner.add(dayLabel, i + 1, 0);
        }

        // Add time slot headers
        for (int i = 0; i < timeSlots.length; i++) {
            Text timeLabel = new Text(timeSlots[i]);
            GridPane.setHalignment(timeLabel, HPos.CENTER);
            GridPane.setValignment(timeLabel, VPos.CENTER);
            planner.add(timeLabel, 0, i + 1);
        }
    }

    private void populateTimetable(String[] days, String[] timeSlots) {
        Random random = new Random();

        String[] courseCodes = courseSchedule.keySet().toArray(new String[0]);
        int courseIndex = 0;

        for (int row = 1; row <= timeSlots.length; row++) {
            for (int col = 1; col <= days.length; col++) {
                if (courseIndex >= courseCodes.length) break;

                String courseCode = courseCodes[courseIndex];
                String courseName = courseSchedule.get(courseCode);

                Button courseButton = new Button(courseCode + "\n" + courseName);
                courseButton.setPrefSize(150, 80); // Set uniform size for all buttons

                ContextMenu sectionMenu = new ContextMenu();
                int numSections = 4 + random.nextInt(4);
                for (int i = 1; i <= numSections; i++) {
                    String section = "Section " + (char) ('A' + (i - 1));
                    MenuItem sectionItem = new MenuItem(section);
                    sectionItem.setOnAction(e -> {
                        selectedCourseCode = courseCode;
                        selectedSection = section;
                        showPopup("Selection", courseCode + " - " + courseName + " selected. Section: " + section);
                    });
                    sectionMenu.getItems().add(sectionItem);
                }

                courseButton.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        sectionMenu.show(courseButton, event.getScreenX(), event.getScreenY());
                    }
                });

                GridPane.setHalignment(courseButton, HPos.CENTER);
                GridPane.setValignment(courseButton, VPos.CENTER);
                planner.add(courseButton, col, row);

                courseIndex++;
                if (courseIndex >= courseCodes.length) break;
            }
        }
    }

    private void populateCourseSchedule() {
        courseSchedule.put("CS101", "Introduction to Programming");
        courseSchedule.put("CS102", "Data Structures");
        courseSchedule.put("CS103", "Database Management");
        courseSchedule.put("CS104", "Operating Systems");
        courseSchedule.put("CS105", "Software Engineering");
        courseSchedule.put("CS106", "Computer Networks");
        courseSchedule.put("CS107", "Artificial Intelligence");
        courseSchedule.put("CS108", "Machine Learning");
        courseSchedule.put("CS109", "Cybersecurity");
        courseSchedule.put("CS110", "Web Development");

        courseSchedule.put("CS111", "Algorithms and Complexity");
        courseSchedule.put("CS112", "Cloud Computing");
        courseSchedule.put("CS113", "Mobile App Development");
        courseSchedule.put("CS114", "Human-Computer Interaction");
        courseSchedule.put("CS115", "Blockchain Technology");

        courseSchedule.put("CS116", "Game Development Fundamentals");
        courseSchedule.put("CS117", "Big Data Analytics");
        courseSchedule.put("CS118", "Internet of Things (IoT)");
        courseSchedule.put("CS119", "Embedded Systems");
        courseSchedule.put("CS120", "Programming Paradigms");

        courseSchedule.put("CS121", "Functional Programming");
        courseSchedule.put("CS122", "DevOps and CI/CD");
        courseSchedule.put("CS123", "Quantum Computing Basics");
        courseSchedule.put("CS124", "Natural Language Processing (NLP)");
        courseSchedule.put("CS125", "Ethical Hacking");

        courseSchedule.put("CS126", "Augmented Reality and Virtual Reality");
        courseSchedule.put("CS127", "Digital Signal Processing");
        courseSchedule.put("CS128", "Compiler Design");
        courseSchedule.put("CS129", "Parallel Computing");
        courseSchedule.put("CS130", "Advanced Operating Systems");
        courseSchedule.put("CS131", "Robotics and Automation");
        courseSchedule.put("CS132", "Deep Learning");
        courseSchedule.put("CS133", "Artificial Neural Networks");
        courseSchedule.put("CS134", "Social Network Analysis");
        courseSchedule.put("CS135", "Information Retrieval Systems");
    }

    private void showPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public String getName() {
        return "Advising UI"; // Name to display in the navigation tree
    }

    @Override
    public Parent getView() {
        return createAdvisingUI(); // Return the UI for the Advising page
    }


    @Override
    public @Nullable Node getSnapshotTarget() {
        return null;
    }

    @Override
    public void reset() {

    }
}
