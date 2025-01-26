package enrollium.client.page.students;

import atlantafx.base.theme.Styles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enrollium.client.page.BasePage;
import enrollium.client.page.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.memory.Volatile;
import enrollium.rpc.client.ClientRPC;
import enrollium.rpc.core.JsonUtils;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
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
        dataRefreshService.setOnSucceeded(e -> updateUI(dataRefreshService.getValue()));
        dataRefreshService.setOnFailed(e -> showNotification("Failed to fetch data: " + e.getSource()
                                                                                         .getException()
                                                                                         .getMessage(), NotificationType.WARNING));
        dataRefreshService.start();

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && dataRefreshService != null) {
                dataRefreshService.cancel();
            }
        });
    }

    private TrimesterData fetchDataFromServer() {
        TrimesterData data = new TrimesterData();
        data.subjects = new ArrayList<>();

        String userId = (String) Volatile.getInstance().get("auth_user_id");
        if (userId == null) {
            showNotification("User not authenticated", NotificationType.WARNING);
            return data;
        }

        try {
            JsonNode response = ClientRPC.getInstance()
                                         .call("Course.getSchedule", JsonUtils.createObject().put("userId", userId))
                                         .blockingGet()
                                         .getParams();

            if (response.has("error")) {
                showNotification(response.get("error").asText(), NotificationType.WARNING);
                return data;
            }

            JsonNode subjectsNode = response.get("subjects");
            if (subjectsNode != null) {
                for (JsonNode subjectNode : subjectsNode) {
                    Subject subject = new Subject();
                    subject.subjectName = subjectNode.get("subjectName").asText();
                    subject.subjectCode = subjectNode.get("subjectCode").asText();
                    subject.subjectType = subjectNode.get("subjectType").asText();
                    subject.courseId    = subjectNode.get("courseId").asText();
                    subject.days        = new ArrayList<>();

                    JsonNode daysNode = subjectNode.get("days");
                    if (daysNode != null) {
                        for (JsonNode dayNode : daysNode) {
                            Day day = new Day();
                            day.day      = dayNode.get("day").asText();
                            day.sections = new ArrayList<>();

                            JsonNode sectionsNode = dayNode.get("sections");
                            if (sectionsNode != null) {
                                for (JsonNode sectionNode : sectionsNode) {
                                    Section section = new Section();
                                    section.sectionCode     = sectionNode.get("sectionCode").asText();
                                    section.timeSlot        = sectionNode.get("timeSlot").asInt();
                                    section.currentCapacity = sectionNode.get("currentCapacity").asInt();
                                    section.maxCapacity     = sectionNode.get("maxCapacity").asInt();
                                    section.sectionId       = sectionNode.get("sectionId").asText();
                                    section.isRegistered    = sectionNode.get("isRegistered").asBoolean();
                                    day.sections.add(section);
                                }
                            }
                            subject.days.add(day);
                        }
                    }
                    data.subjects.add(subject);
                }
            }
        } catch (Exception e) {
            showNotification("Error fetching data: " + e.getMessage(), NotificationType.WARNING);
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

        // Initialize daySubjectsMap with empty lists for each day in DAY_ORDER
        for (String day : DAY_ORDER) {
            daySubjectsMap.put(day, new ArrayList<>());
        }

        // Populate daySubjectsMap based on subjects' actual days
        for (Subject subject : subjects) {
            for (Day day : subject.days) {
                String dayKey = day.day;
                if (DAY_ORDER.contains(dayKey)) {
                    List<Subject> daySubjects = daySubjectsMap.get(dayKey);
                    // Check if subject is not already added for this day
                    if (daySubjects.stream().noneMatch(s -> s.subjectCode.equals(subject.subjectCode))) {
                        daySubjects.add(subject);
                    }
                }
            }
        }

        // Add columns for each day in predefined order
        for (String day : DAY_ORDER) {
            List<Subject> daySubjects  = daySubjectsMap.get(day);
            Set<String>   seenSubjects = new HashSet<>();
            List<Subject> uniqueSubjects = daySubjects.stream()
                                                      .filter(s -> seenSubjects.add(s.subjectCode))
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
                addSubjectColumn(subject.subjectCode, currentCol);
                processSections(subject, currentCol, day);
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
                addSubjectColumn(subject.subjectCode, currentCol);
                processSections(subject, currentCol, day);
                currentCol++;
            }
        } else {
            // Add empty subject column
            addSubjectColumn("", currentCol);
            currentCol++;
        }

        addColumnConstraints(dayStartCol, currentCol);
    }

    private void processSections(Subject subject, int col, String targetDay) {
        for (Day day : subject.days) {
            if (day.day.equals(targetDay)) {
                for (Section section : day.sections) {
                    addSection(section, col, subject.subjectType, subject);
                }
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

    private void addSubjectColumn(String subjectCode, int col) {
        Label subjectHeader = new Label(subjectCode);
        subjectHeader.setWrapText(true);
        subjectHeader.setMinHeight(HEADER_ROW_HEIGHT);
        subjectHeader.setMaxWidth(Double.MAX_VALUE);
        styleSubjectHeader(subjectHeader);
        timetableGrid.add(subjectHeader, col, 1);
    }

    private void addSection(Section section, int col, String subjectType, Subject parentSubject) {
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
        Label sectionLabel = createSectionLabel(section, timeslots[timeslotIndex], parentSubject);
        container.getChildren().add(sectionLabel);
    }

    private Label createSectionLabel(Section section, String timeSlot, Subject course) {
        String capacityText = String.format("%s (%d/%d)", section.sectionCode, section.currentCapacity, section.maxCapacity);

        Label label = new Label(capacityText + (section.isRegistered ? " ★" : ""));
        label.setStyle("-fx-background-color: " + toRgbaString(getSubjectColor(section.sectionId)) + "; " + "-fx-padding: 5; -fx-background-radius: 3;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setTooltip(new Tooltip("Time: " + timeSlot.replace('\n', ' ')));

        label.setOnMouseClicked(e -> handleSectionClick(section, course));
        return label;
    }

    private void handleSectionClick(Section section, Subject course) {
        ObjectNode params = JsonUtils.createObject()
                                     .put("courseId", course.courseId)
                                     .put("sectionId", section.isRegistered ? null : section.sectionId);

        ClientRPC.getInstance()
                 .call("Course.updateRegistration", params)
                 .subscribeOn(Schedulers.io())
                 .subscribe(response -> {
                     if (response.getParams().has("success") && response.getParams().get("success").asBoolean()) {
                         Platform.runLater(() -> {
                             showNotification("Registration updated", NotificationType.SUCCESS);
                             dataRefreshService.restart();
                         });
                     } else {
                         String errorMsg = response.getParams().has("error")
                                           ? response.getParams()
                                                     .get("error")
                                                     .asText()
                                           : "Unknown error occurred";
                         Platform.runLater(() -> showNotification(errorMsg, NotificationType.WARNING));
                     }
                 }, error -> Platform.runLater(() -> showNotification("Error: " + error.getMessage(), NotificationType.WARNING)));
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
        String    subjectCode;
        String    subjectType;
        List<Day> days;
        String    courseId;
    }


    private static class Day {
        String        day;
        List<Section> sections;
    }


    private static class Section {
        String  sectionCode;
        int     timeSlot;
        String  sectionId;
        boolean isRegistered;
        int     currentCapacity;
        int     maxCapacity;
    }
}
