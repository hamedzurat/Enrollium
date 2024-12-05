//package enrollium.client;
//
//import io.reactivex.rxjava3.core.Observable;
//import io.reactivex.rxjava3.disposables.CompositeDisposable;
//import io.reactivex.rxjava3.subjects.PublishSubject;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//
//
//public class MainController {
//    private final PublishSubject<Integer> clickSubject = PublishSubject.create();
//    private final CompositeDisposable     disposables  = new CompositeDisposable();
//    @FXML
//    private       Label                   counterLabel;
//    @FXML
//    private       Button                  incrementButton;
//
//    @FXML
//    public void initialize() {
//        // Set up the counter observable
//        Observable<Integer> counterObservable = clickSubject.scan(0, (count, value) -> count + 1)
//                                                            .distinctUntilChanged() // only change if new
//                                                            .doOnError(error -> Platform.runLater(() -> {
//                                                                counterLabel.setText("Error");
//                                                            }))
//                                                            .retry() // resubscribes after errors
//                                                            .share(); // ot and sharable observable
//
//        // Subscribe to counter updates
//        disposables.add( // for cleaning subscriptions to prevent memory
//                counterObservable.subscribe( // say what will happen when THE button is clicked.
//                        count -> Platform.runLater(() -> {
//                            counterLabel.setText(String.valueOf(count));
//                            System.out.println(count);
//                        }), //
//                        error -> System.err.println("Error in counter stream: " + error) //
//                ));
//
//        // Connect button clicks to the subject
//        incrementButton.setOnAction(event -> clickSubject.onNext(1)); // trigger
//
//        // Set up cleanup when window closes
//        Platform.runLater(() -> {
//            incrementButton.getScene().getWindow().setOnCloseRequest(event -> {
//                if (!disposables.isDisposed()) {
//                    disposables.dispose();
//                }
//            });
//        });
//    }
//}

package enrollium.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.HashMap;

public class MainController {

    @FXML
    public TabPane tabPane;
    @FXML
    public GridPane planner;
    @FXML
    public ComboBox<String> courseDropdown;
    @FXML
    public Button takeCourseButton;
    @FXML
    public Text courseFeedback;
    @FXML
    public ListView<String> sections;
    @FXML
    public Button tradeButton;
    @FXML
    public Text tradeFeedback;
    @FXML
    public ListView<String> notifications;
    @FXML
    public Text badgeCounter;
    @FXML
    public ListView<String> groupMembers;
    @FXML
    public Button compareSchedulesButton;
    @FXML
    public Text scheduleFeedback;

    public final HashMap<String, String> courseSchedule = new HashMap<>();

    @FXML
    public void initialize() {
        populateCourseSchedule();

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] timeSlots = {"8-9 AM", "9-10 AM", "10-11 AM", "11-12 PM", "12-1 PM", "1-2 PM"};

        for (int i = 0; i < days.length; i++) {
            planner.add(new Text(days[i]), i + 1, 0);
        }
        for (int i = 0; i < timeSlots.length; i++) {
            planner.add(new Text(timeSlots[i]), 0, i + 1);
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
            courseButton.setStyle("-fx-background-color: lightblue; -fx-font-weight: bold;");
            courseButton.setTooltip(new Tooltip("Course Code: " + courseCode + "\nCourse Name: " + courseName));
            planner.add(courseButton, position[0], position[1]);

            courseIndex++;
        }

        courseDropdown.getItems().addAll(courseSchedule.keySet());
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
