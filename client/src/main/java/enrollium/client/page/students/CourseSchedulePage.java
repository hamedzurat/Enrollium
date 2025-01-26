package enrollium.client.page.students;

import atlantafx.base.theme.Styles;
import enrollium.client.page.BasePage;
import enrollium.client.page.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;


public class CourseSchedulePage extends BasePage {
    public static final  TranslationKey                  NAME                     = TranslationKey.SectionSelection;
    private static final String[]                        THEORY_TIMESLOTS         = {"8:30\n -\n9:50", "9:51\n -\n11:10", "11:11\n  -\n12:30", "12:31\n  -\n13:50", "13:51\n  -\n15:10", "15:11\n  -\n16:30"};
    private static final String[]                        LAB_TIMESLOTS            = {"8:30\n -\n11:10", "11:11\n -\n13:50", "13:51\n -\n16:30"};
    private static final List<String>                    DAY_ORDER                = Arrays.asList("Sat", "Sat+Tue", "Sun", "Sun+Wed", "Tue", "Wed");
    private static final double                          HEADER_ROW_HEIGHT        = 30;
    private static final double                          TIMESLOT_ROW_MIN_HEIGHT  = 80;
    private static final double                          TIMESLOT_ROW_PREF_HEIGHT = 100;
    private static final double                          TIME_COLUMN_WIDTH        = 80;
    private static final double                          MIN_CELL_WIDTH           = 100;
    private static final double                          MAX_CELL_WIDTH           = 150;
    private static final double                          CELL_PADDING             = 5;
    private final        GridPane                        timetableGrid            = new GridPane();
    private final        Map<String, Color>              subjectColors            = new HashMap<>();
    private              ScheduledService<TrimesterData> dataRefreshService;
    private              int                             currentCol               = 1;

    public CourseSchedulePage() {
        addPageHeader();
        addNode(setupTimetable());
        setupDataRefresh();
    }

    private ScrollPane setupTimetable() {
        timetableGrid.getStyleClass().add("timetable-grid");
        timetableGrid.setHgap(1);
        timetableGrid.setVgap(1);
        timetableGrid.setGridLinesVisible(true);
        timetableGrid.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        timetableGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        setupRowConstraints();
        setupTimeColumn();

        ScrollPane scrollPane = new ScrollPane(timetableGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinViewportWidth(Region.USE_COMPUTED_SIZE);
        scrollPane.setMinViewportWidth(800);

        return scrollPane;
    }

    private void setupDataRefresh() {
        dataRefreshService = new ScheduledService<>() {
            @Override
            protected Task<TrimesterData> createTask() {
                return new Task<>() {
                    @Override
                    protected TrimesterData call() {
                        return fetchDataFromServer();
                    }
                };
            }
        };

        dataRefreshService.setPeriod(Duration.seconds(8));
        dataRefreshService.setOnSucceeded(_ -> updateUI(dataRefreshService.getValue()));
        dataRefreshService.setOnFailed(e -> showNotification("Failed to fetch data: " + e.getSource()
                                                                                         .getException(), NotificationType.WARNING));
        dataRefreshService.start();
    }

    private TrimesterData fetchDataFromServer() {
        TrimesterData data = new TrimesterData();
        data.subjects = new ArrayList<>();
        Random rand = new Random();

        // Generate fixed set of subjects
        List<Subject> theorySubjects = new ArrayList<>();
        List<Subject> labSubjects    = new ArrayList<>();

        int numOfTheorySub = rand.nextInt(0, 5);
        for (int i = 0; i < numOfTheorySub; i++) {
            Subject subject = new Subject();
            subject.subjectName = "Theory " + (i + 1);
            subject.subjectType = "THEORY";
            subject.days        = new ArrayList<>();
            theorySubjects.add(subject);
        }

        int numOfLabSub = rand.nextInt(0, 5);
        for (int i = 0; i < numOfLabSub; i++) {
            Subject subject = new Subject();
            subject.subjectName = "Lab " + (i + 1);
            subject.subjectType = "LAB";
            subject.days        = new ArrayList<>();
            labSubjects.add(subject);
        }

        // Assign days and sections
        for (Subject subject : theorySubjects) {
            // Theory subjects appear in all theory days
            for (String day : DAY_ORDER.stream().filter(d -> d.contains("+")).toList()) {
                Day dayEntry = new Day();
                dayEntry.day      = day;
                dayEntry.sections = new ArrayList<>();

                // Generate random sections
                int sectionCount = rand.nextInt(0, 6);
                for (int k = 0; k < sectionCount; k++) {
                    Section section = new Section();
                    section.sectionCode = String.valueOf((char) ('A' + k));
                    section.timeSlot    = rand.nextInt(6) + 1; // 1-6
                    dayEntry.sections.add(section);
                }
                subject.days.add(dayEntry);
            }
            data.subjects.add(subject);
        }

        for (Subject subject : labSubjects) {
            // Lab subjects appear in all lab days
            for (String day : DAY_ORDER.stream().filter(d -> !d.contains("+")).toList()) {
                Day dayEntry = new Day();
                dayEntry.day      = day;
                dayEntry.sections = new ArrayList<>();

                // Generate random sections
                int sectionCount = rand.nextInt(0, 6);
                for (int k = 0; k < sectionCount; k++) {
                    Section section = new Section();
                    section.sectionCode = String.valueOf((char) ('A' + k));
                    section.timeSlot    = rand.nextInt(3) + 1; // 1-3
                    dayEntry.sections.add(section);
                }
                subject.days.add(dayEntry);
            }
            data.subjects.add(subject);
        }

        return data;
    }

    private void updateUI(TrimesterData data) {
        Platform.runLater(() -> {
            clearTimetable();
            if (data != null && data.subjects != null) {
                processSubjects(data.subjects);
                updateAllRowHeights();
            } else {
                showNotification("No schedule data available", NotificationType.INFO);
            }
        });
    }

    private void updateAllRowHeights() {
        Map<Integer, Double> rowHeights = new HashMap<>();

        timetableGrid.getChildren().stream().filter(node -> node instanceof VBox).forEach(node -> {
            Integer row = GridPane.getRowIndex(node);
            if (row != null && row >= 2) { // Skip headers (rows 0-1)
                VBox   vbox   = (VBox) node;
                double height = vbox.getChildren().size() * 30 + 20;
                rowHeights.merge(row, height, Math::max);
            }
        });

        rowHeights.forEach((row, height) -> {
            if (row >= timetableGrid.getRowConstraints().size()) return;
            RowConstraints rc = timetableGrid.getRowConstraints().get(row);
            rc.setMinHeight(Math.max(height, TIMESLOT_ROW_MIN_HEIGHT));
            rc.setPrefHeight(Math.max(height, TIMESLOT_ROW_PREF_HEIGHT));
        });
    }

    private void clearTimetable() {
        timetableGrid.getChildren()
                     .removeIf(node -> GridPane.getColumnIndex(node) == null || GridPane.getColumnIndex(node) != 0);
        timetableGrid.getColumnConstraints().clear();
        timetableGrid.getRowConstraints().clear();

        setupRowConstraints(); // Re-add headers and timeslot rows
        setupTimeColumn();
        currentCol = 1;
    }

    private void setupRowConstraints() {
        // Header rows
        for (int i = 0; i < 2; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(HEADER_ROW_HEIGHT);
            rc.setPrefHeight(HEADER_ROW_HEIGHT);
            rc.setVgrow(Priority.NEVER);
            timetableGrid.getRowConstraints().add(rc);
        }

        // Timeslot rows
        for (int i = 0; i < THEORY_TIMESLOTS.length; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(TIMESLOT_ROW_MIN_HEIGHT);
            rc.setPrefHeight(TIMESLOT_ROW_PREF_HEIGHT);
            rc.setVgrow(Priority.ALWAYS);
            timetableGrid.getRowConstraints().add(rc);
        }
    }

    private void setupTimeColumn() {
        Label timeHeader = new Label("TIME");
        timeHeader.getStyleClass().addAll(Styles.TITLE_3, Styles.TEXT_BOLD);
        timeHeader.getStyleClass().add("time-header");
        timeHeader.setAlignment(Pos.CENTER);
        timeHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        timetableGrid.add(timeHeader, 0, 0, 1, 2);

        // Add theory time labels
        for (int i = 0; i < THEORY_TIMESLOTS.length; i++) {
            Label timeLabel = new Label(THEORY_TIMESLOTS[i]);
            timeLabel.getStyleClass().addAll(Styles.TITLE_3, Styles.TEXT_BOLD);
            timeLabel.setStyle(getTimeHeaderStyle(i));
            timeLabel.setPrefWidth(TIME_COLUMN_WIDTH);
            timeLabel.setAlignment(Pos.CENTER);
            timeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            timetableGrid.add(timeLabel, 0, i + 2);
        }

        ColumnConstraints timeCC = new ColumnConstraints();
        timeCC.setMinWidth(TIME_COLUMN_WIDTH);
        timeCC.setPrefWidth(TIME_COLUMN_WIDTH);
        timeCC.setHgrow(Priority.NEVER);
        timetableGrid.getColumnConstraints().add(timeCC);
    }

    private void processSubjects(List<Subject> subjects) {
        Map<String, List<Subject>> daySubjectsMap = new LinkedHashMap<>();

        // Initialize all days with appropriate subject types
        for (String day : DAY_ORDER) {
            List<Subject> daySubjects = new ArrayList<>();

            if (day.contains("+")) { // Theory day
                daySubjects.addAll(subjects.stream().filter(s -> s.subjectType.equals("THEORY")).toList());
            } else { // Lab day
                daySubjects.addAll(subjects.stream().filter(s -> s.subjectType.equals("LAB")).toList());
            }

            daySubjectsMap.put(day, daySubjects);
        }

        // Add columns for each day in predefined order
        for (Map.Entry<String, List<Subject>> entry : daySubjectsMap.entrySet()) {
            String        day         = entry.getKey();
            List<Subject> daySubjects = entry.getValue();

            // Remove duplicates while maintaining order
            Set<String> seenSubjects = new HashSet<>();
            List<Subject> uniqueSubjects = daySubjects.stream()
                                                      .filter(s -> seenSubjects.add(s.subjectName))
                                                      .collect(Collectors.toList());

            if (day.contains("+")) {
                addTheoryDay(day, uniqueSubjects);
            } else {
                addLabDay(day, uniqueSubjects);
            }
        }
    }

    private void addLabDay(String day, List<Subject> subjects) {
        Label dayHeader = new Label(day);
        styleHeader(dayHeader, Styles.BG_ACCENT_SUBTLE);
        int dayStartCol = currentCol;

        // Always add header even if no subjects
        int subjectCount = Math.max(subjects.size(), 1);
        timetableGrid.add(dayHeader, currentCol, 0, subjectCount, 1);

        if (!subjects.isEmpty()) {
            for (Subject subject : subjects) {
                addSubjectColumn(subject.subjectName, currentCol);
                processSections(subject, currentCol);
                currentCol++;
            }
        } else {
            // Add empty subject column
            addSubjectColumn("", currentCol);
            currentCol++;
        }

        addColumnConstraints(dayStartCol, currentCol);
    }

    private void addTheoryDay(String day, List<Subject> subjects) {
        Label dayHeader = new Label(day);
        styleHeader(dayHeader, Styles.BG_DANGER_SUBTLE);
        int dayStartCol = currentCol;

        // Always add header even if no subjects
        int subjectCount = Math.max(subjects.size(), 1);
        timetableGrid.add(dayHeader, currentCol, 0, subjectCount, 1);

        if (!subjects.isEmpty()) {
            for (Subject subject : subjects) {
                addSubjectColumn(subject.subjectName, currentCol);
                processSections(subject, currentCol);
                currentCol++;
            }
        } else {
            // Add empty subject column
            addSubjectColumn("", currentCol);
            currentCol++;
        }

        addColumnConstraints(dayStartCol, currentCol);
    }

    private void processSections(Subject subject, int col) {
        for (Day day : subject.days) {
            for (Section section : day.sections) {
                addSection(section, col, subject.subjectType);
            }
        }
    }

    private void addColumnConstraints(int startCol, int endCol) {
        for (int i = startCol; i < endCol; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(MIN_CELL_WIDTH);
            cc.setPrefWidth(MIN_CELL_WIDTH);
            cc.setMaxWidth(MAX_CELL_WIDTH);
            cc.setHgrow(Priority.SOMETIMES);
            timetableGrid.getColumnConstraints().add(cc);
        }
    }

    private void addSubjectColumn(String subjectName, int col) {
        Label subjectHeader = new Label(subjectName);
        subjectHeader.setWrapText(true);
        subjectHeader.setMinHeight(HEADER_ROW_HEIGHT);
        subjectHeader.setMaxWidth(Double.MAX_VALUE);
        styleSubjectHeader(subjectHeader);
        timetableGrid.add(subjectHeader, col, 1);
    }

    private void addSection(Section section, int col, String subjectType) {
        int timeslotIndex = section.timeSlot - 1; // Convert to 0-based
        if (timeslotIndex < 0) return;

        int      rowIndex;
        int      rowSpan;
        String[] timeslots;

        if ("LAB".equals(subjectType)) {
            timeslots = LAB_TIMESLOTS;
            if (timeslotIndex >= LAB_TIMESLOTS.length) return;
            rowSpan  = 2;
            rowIndex = (timeslotIndex * 2) + 2; // Map to correct theory rows
        } else {
            timeslots = THEORY_TIMESLOTS;
            if (timeslotIndex >= THEORY_TIMESLOTS.length) return;
            rowSpan  = 1;
            rowIndex = timeslotIndex + 2;
        }

        // Ensure row exists
        while (timetableGrid.getRowConstraints().size() <= rowIndex + rowSpan) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(50);
            rc.setPrefHeight(100);
            rc.setVgrow(Priority.ALWAYS);
            timetableGrid.getRowConstraints().add(rc);
        }

        VBox  container    = getOrCreateCellContainer(col, rowIndex, rowSpan);
        Label sectionLabel = createSectionLabel(section, timeslots[timeslotIndex]);
        container.getChildren().add(sectionLabel);
    }

    private Label createSectionLabel(Section section, String timeSlot) {
        Label label = new Label(section.sectionCode);
        label.setStyle("-fx-background-color: " + toRgbaString(getSubjectColor(section.sectionCode)) + "; -fx-padding: 5; -fx-background-radius: 3;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);

        String info = String.format("Section %s | Time: %s", section.sectionCode, timeSlot);
        label.setOnMouseClicked(_ -> showNotification(info, NotificationType.INFO));
        return label;
    }

    private VBox getOrCreateCellContainer(int col, int row, int rowSpan) {
        for (Node node : timetableGrid.getChildren()) {
            Integer nodeCol = GridPane.getColumnIndex(node);
            Integer nodeRow = GridPane.getRowIndex(node);
            if (node instanceof VBox && nodeCol != null && nodeCol == col && nodeRow != null && nodeRow == row) {
                return (VBox) node;
            }
        }

        VBox container = new VBox(5);
        container.setPadding(new Insets(5));
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(150);
        container.setPrefWidth(100);
        container.setMinWidth(100);
        container.getStyleClass().add("grid-cell");

        timetableGrid.add(container, col, row, 1, rowSpan);
        return container;
    }

    private void styleHeader(Label header, String color) {
        header.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD, color);
        header.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
    }

    private void styleSubjectHeader(Label header) {
        header.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD, Styles.BG_SUCCESS_SUBTLE);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(CELL_PADDING));
    }

    private Color getSubjectColor(String subject) {
        return subjectColors.computeIfAbsent(subject, _ -> generateRandomPastelColor());
    }

    private Color generateRandomPastelColor() {
        double hue = RANDOM.nextDouble() * 360;
        return Color.hsb(hue, 0.3, 0.9, 0.5);
    }

    private String toRgbaString(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255), color.getOpacity());
    }

    private Color generateTimeHeaderColor(int index) {
        // Create alternating colors based on index
        double baseHue    = (index * 60) % 360;  // 60° steps for distinct colors
        double saturation = 0.2;
        double brightness = index % 2 == 0 ? 0.95 : 0.90; // Alternate brightness
        return Color.hsb(baseHue, saturation, brightness, 0.8);
    }

    private String getTimeHeaderStyle(int index) {
        Color bgColor = generateTimeHeaderColor(index);
        return String.format("-fx-background-color: %s; -fx-border-color: rgba(0,0,0,0.2); -fx-border-width: 0.5;", toRgbaString(bgColor));
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    // Data model classes
    private static class TrimesterData {
        List<Subject> subjects;
    }


    private static class Subject {
        String    subjectName;
        String    subjectType;
        List<Day> days;
    }


    private static class Day {
        String        day;
        List<Section> sections;
    }


    private static class Section {
        String sectionCode;
        int    timeSlot;
    }
}
