package enrollium.client.page.students;

import atlantafx.base.theme.Styles;
import enrollium.client.page.BasePage;
import enrollium.client.page.general.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;


public class CourseSchedulePage extends BasePage {
    public static final  TranslationKey     NAME                   = TranslationKey.HELLO;
    private static final String[]           TIMESLOTS              = {"8:30\n -\n9:50", "9:51\n -\n11:10", "11:11\n  -\n12:30", "12:31\n  -\n13:50", "13:51\n  -\n15:10", "15:11\n  -\n16:30"};
    private final        List<String>       LAB_SUBJECTS           = Arrays.asList("AOOP-LAB", "Phy-LAB");
    private final        List<String>       THEORY_SUBJECTS        = Arrays.asList("Phy", "Vec", "Circ");
    private final        GridPane           timetableGrid          = new GridPane();
    private final        Map<String, Color> subjectColors          = new HashMap<>();
    private              int                currentCol             = 1;

    public CourseSchedulePage() {
        addPageHeader();

        addNode(setupTimetable());
        generateMockData();

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    timetableGrid.requestLayout();
                    timetableGrid.applyCss();
                });
            }
        });

        Platform.runLater(() -> {
            timetableGrid.requestLayout();
            timetableGrid.applyCss();
        });
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private ScrollPane setupTimetable() {
        timetableGrid.setHgap(1);
        timetableGrid.setVgap(1);
        timetableGrid.setGridLinesVisible(true);
        timetableGrid.setMinWidth(Region.USE_COMPUTED_SIZE);
        timetableGrid.setMinHeight(Region.USE_COMPUTED_SIZE);
        timetableGrid.setPrefWidth(Region.USE_COMPUTED_SIZE);
        timetableGrid.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Configure row constraints
        // Header rows
        for (int i = 0; i < 2; i++) {
            RowConstraints headerRC = new RowConstraints();
            headerRC.setMinHeight(30);
            headerRC.setPrefHeight(30);
            headerRC.setVgrow(Priority.NEVER);
            timetableGrid.getRowConstraints().add(headerRC);
        }

        // Timeslot rows - will grow as needed
        for (int i = 0; i < TIMESLOTS.length; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(50);  // Default value
            rc.setPrefHeight(Region.USE_COMPUTED_SIZE);  // Allow JavaFX to compute
            rc.setVgrow(Priority.ALWAYS);  // Allow growth based on content
            timetableGrid.getRowConstraints().add(rc);
        }

        setupTimeColumn();
        addDayColumns();

        ScrollPane scrollPane = new ScrollPane(timetableGrid);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private void setupTimeColumn() {
        // Add "TIME" label at top-left corner
        Label timeHeader = new Label("TIME");
        timeHeader.getStyleClass().addAll(Styles.TITLE_3, Styles.TEXT_BOLD);
        timeHeader.getStyleClass().add("time-header");

        timeHeader.setAlignment(Pos.CENTER);
        timeHeader.setMaxWidth(Double.MAX_VALUE);
        timeHeader.setMaxHeight(Double.MAX_VALUE);
        timetableGrid.add(timeHeader, 0, 0, 1, 2); // Spans both header rows

        // Add time labels
        for (int i = 0; i < TIMESLOTS.length; i++) {
            Label timeLabel = new Label(TIMESLOTS[i]);
            timeHeader.getStyleClass().addAll(Styles.TITLE_3, Styles.TEXT_BOLD);
            timeLabel.setPrefWidth(80);
            timeLabel.setAlignment(Pos.CENTER);
            timetableGrid.add(timeLabel, 0, i + 2); // +2 for header rows
        }

        // Time column constraints
        ColumnConstraints timeCC = new ColumnConstraints();
        timeCC.setMinWidth(80);
        timeCC.setPrefWidth(80);
        timeCC.setHgrow(Priority.NEVER);
        timetableGrid.getColumnConstraints().add(timeCC);
    }

    private void addDayColumns() {
        addLabDay("Sat");
        addTheoryDay("Sat+Thu");
        addLabDay("Sun");
        addTheoryDay("Sun+Wed");
        addLabDay("Tue");
        addLabDay("Wed");
    }

    private void addLabDay(String day) {
        // Day header
        Label dayHeader = new Label(day);
        styleHeader(dayHeader, Styles.BG_ACCENT_SUBTLE);
        int dayStartCol = currentCol;
        timetableGrid.add(dayHeader, currentCol, 0, LAB_SUBJECTS.size(), 1);

        // Subject headers and columns
        for (String subject : LAB_SUBJECTS) addSubjectColumn(subject, currentCol++);

        // Set column constraints for the entire day
        for (int i = dayStartCol; i < currentCol; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(100);
            cc.setPrefWidth(100);
            cc.setHgrow(Priority.SOMETIMES);
            timetableGrid.getColumnConstraints().add(cc);
        }
    }

    private void addTheoryDay(String day) {
        // Day header
        Label dayHeader = new Label(day);
        styleHeader(dayHeader, Styles.BG_DANGER_SUBTLE);
        int dayStartCol = currentCol;
        timetableGrid.add(dayHeader, currentCol, 0, THEORY_SUBJECTS.size(), 1);

        // Subject headers and columns
        for (String subject : THEORY_SUBJECTS) addSubjectColumn(subject, currentCol++);

        // Set column constraints for the entire day
        for (int i = dayStartCol; i < currentCol; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(100);
            cc.setPrefWidth(100);
            cc.setHgrow(Priority.SOMETIMES);
            timetableGrid.getColumnConstraints().add(cc);
        }
    }

    private void addSubjectColumn(String subject, int col) {
        Label subjectHeader = new Label(subject);
        styleSubjectHeader(subjectHeader);
        timetableGrid.add(subjectHeader, col, 1);
    }

    private void addSection(Section section, int col) {
        VBox container = getOrCreateCellContainer(col, section.timeslotIndex + 2);

        Label sectionLabel = new Label(section.code);
        sectionLabel.setStyle("-fx-background-color: " + toRgbaString(getSubjectColor(section.subject)) + ";" + "-fx-padding: 5;" + "-fx-background-radius: 3;");
        sectionLabel.setMaxWidth(Double.MAX_VALUE);
        sectionLabel.setAlignment(Pos.CENTER);

        // Add click event handler to display notification
        sectionLabel.setOnMouseClicked(e -> {
            String info = String.format("Section %s | Subject: %s | Time: %s | Day: %s", section.code, section.subject, TIMESLOTS[section.timeslotIndex], section.weekday);
            showNotification(info, NotificationType.INFO);
        });

        // Add section label to container
        container.getChildren().add(sectionLabel);

        // Use layoutBoundsProperty listener to trigger row height adjustment after rendering
        sectionLabel.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            updateRowHeight(section.timeslotIndex);
        });
    }

    private void updateRowHeight(int timeslotIndex) {
        Platform.runLater(() -> {
            try {
                RowConstraints rowConstraints = timetableGrid.getRowConstraints()
                                                             .get(timeslotIndex + 2); // Skip headers

                double maxHeight = timetableGrid.getChildren()
                                                .stream()
                                                .filter(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == (timeslotIndex + 2))
                                                .filter(node -> node instanceof VBox)
                                                .mapToDouble(node -> node.getBoundsInLocal().getHeight())
                                                .max()
                                                .orElse(50); // Default height if no content

                if (rowConstraints.getPrefHeight() < maxHeight + 10) {
                    rowConstraints.setMinHeight(maxHeight + 10);
                    rowConstraints.setPrefHeight(maxHeight + 10);

                    // Ensure single layout refresh after all updates
                    timetableGrid.requestLayout();
                }
            } catch (Exception e) {
                System.err.println("Error updating row height: " + e.getMessage());
            }
        });
    }

    private VBox getOrCreateCellContainer(int col, int row) {
        for (Node node : timetableGrid.getChildren()) {
            if (node instanceof VBox && GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return (VBox) node;
            }
        }

        VBox container = new VBox(5);  // Ensure spacing of 5 between stacked items
        container.setPadding(new Insets(5));  // Add padding to prevent touching edges
        container.setAlignment(Pos.TOP_CENTER);
        timetableGrid.add(container, col, row);
        return container;
    }

    private void generateMockData() {
        initializeSubjectColors();

        int subjectCol = 1;
        for (String day : Arrays.asList("Sat", "Sat+Thu", "Sun", "Sun+Wed", "Tue", "Wed")) {
            List<String> subjects = day.contains("+") ? THEORY_SUBJECTS : LAB_SUBJECTS;
            for (String subject : subjects) {
                generateMockSections(subject, day, subjectCol);
                subjectCol++;
            }
        }

        // Schedule layout update once after all sections are added
        Platform.runLater(timetableGrid::requestLayout);
    }

    private void generateMockSections(String subject, String day, int col) {
        List<Integer> timeslots    = new ArrayList<>();
        int           numTimeslots = RANDOM.nextInt(3) + 2; // 2-4 timeslots
        while (timeslots.size() < numTimeslots) {
            int slot = RANDOM.nextInt(TIMESLOTS.length);
            if (!timeslots.contains(slot)) timeslots.add(slot);
        }

        for (int timeslot : timeslots) {
            int numSections = RANDOM.nextInt(6); // 1-4 sections per timeslot
            for (int i = 0; i < numSections; i++) {
                String  sectionCode = String.valueOf((char) ('A' + RANDOM.nextInt(26)));
                Section section     = new Section(sectionCode, subject, timeslot, day);
                addSection(section, col);
            }
        }
    }

    private void initializeSubjectColors() {
        for (String subject : LAB_SUBJECTS) {
            subjectColors.put(subject, generateRandomPastelColor());
        }
        for (String subject : THEORY_SUBJECTS) {
            subjectColors.put(subject, generateRandomPastelColor());
        }
    }

    private void styleHeader(Label header, String color) {
        header.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD, color);
        header.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
    }

    private void styleSubjectHeader(Label header) {
        header.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD, Styles.BG_SUCCESS_SUBTLE);
        header.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
    }

    private Color getSubjectColor(String subject) {
        return subjectColors.computeIfAbsent(subject, k -> generateRandomPastelColor());
    }

    private Color generateRandomPastelColor() {
        double hue = RANDOM.nextDouble() * 360;
        return Color.hsb(hue, 0.3, 0.9, 0.5);
    }

    private String toRgbaString(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255), color.getOpacity());
    }

    private static class Section {
        String code;
        String subject;
        int    timeslotIndex;
        String weekday;

        Section(String code, String subject, int timeslotIndex, String weekday) {
            this.code          = code;
            this.subject       = subject;
            this.timeslotIndex = timeslotIndex;
            this.weekday       = weekday;
        }
    }
}
