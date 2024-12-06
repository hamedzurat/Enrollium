package enrollium.client;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

import java.util.HashMap;

public class MainController {
    public final HashMap<String, String> courseSchedule = new HashMap<>();

    private String selectedCourseCode = null;

    @FXML
    public GridPane planner;
    @FXML
    public Button takeCourseButton;
    @FXML
    public Text courseFeedback;

    @FXML
    public void initialize() {
        populateCourseSchedule();

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] timeSlots = {"8-9 AM", "9-10 AM", "10-11 AM", "11-12 PM", "12-1 PM", "1-2 PM"};

        for (int i = 0; i <= days.length; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / (days.length + 1));
            planner.getColumnConstraints().add(columnConstraints);
        }

        for (int i = 0; i <= timeSlots.length; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / (timeSlots.length + 1));
            planner.getRowConstraints().add(rowConstraints);
        }

        for (int i = 0; i < days.length; i++) {
            Text dayLabel = new Text(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            GridPane.setValignment(dayLabel, VPos.CENTER);
            planner.add(dayLabel, i + 1, 0);
        }

        for (int i = 0; i < timeSlots.length; i++) {
            Text timeLabel = new Text(timeSlots[i]);
            timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            GridPane.setHalignment(timeLabel, HPos.CENTER);
            GridPane.setValignment(timeLabel, VPos.CENTER);
            planner.add(timeLabel, 0, i + 1);
        }

        int[][] coursePositions = {
                {1, 1}, {2, 1}, {3, 1}, {4, 1}, {5, 1},
                {1, 2}, {2, 2}, {3, 2}, {4, 2}, {5, 2},
                {1, 3}, {2, 3}, {3, 3}, {4, 3}, {5, 3},
                {1, 4}, {2, 4}, {3, 4}, {4, 4}, {5, 4},
                {1, 5}, {2, 5}, {3, 5}, {4, 5}, {5, 5},
                {1, 6}, {2, 6}, {3, 6}, {4, 6}, {5, 6}
        };

        int courseIndex = 0;
        String[] courseCodes = courseSchedule.keySet().toArray(new String[0]);
        for (int[] position : coursePositions) {
            if (courseIndex >= courseCodes.length) break;

            String courseCode = courseCodes[courseIndex];
            String courseName = courseSchedule.get(courseCode);

            Button courseButton = new Button(courseCode + "\n" + courseName);
            courseButton.setStyle(
                    "-fx-background-color: lightblue; " +
                    "-fx-font-weight: bold; " +
                    "-fx-alignment: center; " +
                    "-fx-text-alignment: center;"
            );
            courseButton.setTooltip(new Tooltip("Course Code: " + courseCode + "\nCourse Name: " + courseName));

            courseButton.setOnAction(event -> selectedCourseCode = courseCode);

            GridPane.setHalignment(courseButton, HPos.CENTER); // Center horizontally
            GridPane.setValignment(courseButton, VPos.CENTER); // Center vertically
            planner.add(courseButton, position[0], position[1]);

            courseIndex++;
        }

        takeCourseButton.setOnAction(event -> {
            if (selectedCourseCode != null) {
                String selectedCourseName = courseSchedule.get(selectedCourseCode);
                courseFeedback.setText("You have selected " + selectedCourseName + " to take.");
            } else {
                courseFeedback.setText("No course selected.");
            }
        });
    }

    public void populateCourseSchedule() {
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
}
