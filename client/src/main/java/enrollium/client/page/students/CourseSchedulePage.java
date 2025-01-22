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
    public static final  TranslationKey        NAME                     = TranslationKey.HELLO;
    private static final String[]              THEORY_TIMESLOTS         = {"8:30\n -\n9:50", "9:51\n -\n11:10", "11:11\n  -\n12:30", "12:31\n  -\n13:50", "13:51\n  -\n15:10", "15:11\n  -\n16:30"};
    private static final String[]              LAB_TIMESLOTS            = {"8:30\n -\n11:10", "11:11\n -\n13:50", "13:51\n -\n16:30"};
    private static final Map<Integer, Integer> LAB_TO_THEORY_SLOTS      = Map.of(0, 0,  // First lab slot starts at first theory slot
            1, 2,  // Second lab slot starts at third theory slot
            2, 4   // Third lab slot starts at fifth theory slot
    );
    private static final int                   MIN_SECTIONS_PER_SLOT    = 1; // Minimum sections per timeslot
    private static final int                   MAX_ADDITIONAL_SECTIONS  = 5; // Additional random sections
    private static final int                   MIN_TIMESLOTS            = 3; // Minimum timeslots to fill
    private static final int                   MAX_ADDITIONAL_TIMESLOTS = 3; // Additional random timeslots
    private final        List<String>          LAB_SUBJECTS             = Arrays.asList("AOOP-LAB", "Phy-LAB", "DLD lab");
    private final        List<String>          THEORY_SUBJECTS          = Arrays.asList("Phy", "Vec", "Circ", "DLD");
    private final        GridPane              timetableGrid            = new GridPane();
    private final        Map<String, Color>    subjectColors            = new HashMap<>();
    private              int                   currentCol               = 1;

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

        // Set size constraints for the grid
        timetableGrid.setMinWidth(Region.USE_COMPUTED_SIZE);
        timetableGrid.setMaxWidth(Region.USE_PREF_SIZE);
        timetableGrid.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // Ensure grid can grow vertically
        timetableGrid.setMinHeight(Region.USE_COMPUTED_SIZE);
        timetableGrid.setMaxHeight(Double.MAX_VALUE);

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
        for (int i = 0; i < THEORY_TIMESLOTS.length; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(50);  // Minimum height for readability
            rc.setPrefHeight(100); // Preferred height to show content
            rc.setMaxHeight(Region.USE_COMPUTED_SIZE);
            rc.setVgrow(Priority.ALWAYS);
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
        scrollPane.setMinViewportWidth(800);
        scrollPane.setMinWidth(800);

        return scrollPane;
    }

    private void setupTimeColumn() {
        Label timeHeader = new Label("TIME");
        timeHeader.getStyleClass().addAll(Styles.TITLE_3, Styles.TEXT_BOLD);
        timeHeader.getStyleClass().add("time-header");
        timeHeader.setAlignment(Pos.CENTER);
        timeHeader.setMaxWidth(Double.MAX_VALUE);
        timeHeader.setMaxHeight(Double.MAX_VALUE);
        timetableGrid.add(timeHeader, 0, 0, 1, 2);

        // Add theory time labels
        for (int i = 0; i < THEORY_TIMESLOTS.length; i++) {
            Label timeLabel = new Label(THEORY_TIMESLOTS[i]);
            timeLabel.getStyleClass().addAll(Styles.TITLE_3, Styles.TEXT_BOLD);
            timeLabel.setPrefWidth(80);
            timeLabel.setAlignment(Pos.CENTER);
            timetableGrid.add(timeLabel, 0, i + 2);
        }

        ColumnConstraints timeCC = new ColumnConstraints();
        timeCC.setMinWidth(80);
        timeCC.setPrefWidth(80);
        timeCC.setMaxWidth(100);
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
        Label dayHeader = new Label(day);
        styleHeader(dayHeader, Styles.BG_ACCENT_SUBTLE);
        int dayStartCol = currentCol;
        timetableGrid.add(dayHeader, currentCol, 0, LAB_SUBJECTS.size(), 1);

        for (String subject : LAB_SUBJECTS) {
            addSubjectColumn(subject, currentCol++);
        }

        for (int i = dayStartCol; i < currentCol; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(100);
            cc.setPrefWidth(100);
            cc.setMaxWidth(150);
            cc.setHgrow(Priority.NEVER);
            timetableGrid.getColumnConstraints().add(cc);
        }
    }

    private void addTheoryDay(String day) {
        Label dayHeader = new Label(day);
        styleHeader(dayHeader, Styles.BG_DANGER_SUBTLE);
        int dayStartCol = currentCol;
        timetableGrid.add(dayHeader, currentCol, 0, THEORY_SUBJECTS.size(), 1);

        for (String subject : THEORY_SUBJECTS) {
            addSubjectColumn(subject, currentCol++);
        }

        for (int i = dayStartCol; i < currentCol; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(100);
            cc.setPrefWidth(100);
            cc.setMaxWidth(150);
            cc.setHgrow(Priority.NEVER);
            timetableGrid.getColumnConstraints().add(cc);
        }
    }

    private void addSubjectColumn(String subject, int col) {
        Label subjectHeader = new Label(subject);
        styleSubjectHeader(subjectHeader);
        timetableGrid.add(subjectHeader, col, 1);
    }

    private void addSection(Section section, int col) {
        int    rowIndex;
        int    rowSpan;
        String timeSlot;

        if (LAB_SUBJECTS.contains(section.subject)) {
            rowIndex = LAB_TO_THEORY_SLOTS.get(section.timeslotIndex) + 2;
            rowSpan  = 2;
            timeSlot = LAB_TIMESLOTS[section.timeslotIndex];
        } else {
            rowIndex = section.timeslotIndex + 2;
            rowSpan  = 1;
            timeSlot = THEORY_TIMESLOTS[section.timeslotIndex];
        }

        VBox container = getOrCreateCellContainer(col, rowIndex, rowSpan);

        Label sectionLabel = new Label(section.code);
        sectionLabel.setStyle("-fx-background-color: " + toRgbaString(getSubjectColor(section.subject)) + ";" + "-fx-padding: 5;" + "-fx-background-radius: 3;");
        sectionLabel.setMaxWidth(Double.MAX_VALUE);
        sectionLabel.setAlignment(Pos.CENTER);

        sectionLabel.setOnMouseClicked(e -> {
            String info = String.format("Section %s | Subject: %s | Time: %s | Day: %s", section.code, section.subject, timeSlot, section.weekday);
            showNotification(info, NotificationType.INFO);
        });

        container.getChildren().add(sectionLabel);
    }

    private VBox getOrCreateCellContainer(int col, int row, int rowSpan) {
        for (Node node : timetableGrid.getChildren()) {
            if (node instanceof VBox && GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return (VBox) node;
            }
        }

        VBox container = new VBox(5);
        container.setPadding(new Insets(5));
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(150);
        container.setPrefWidth(100);
        container.setMinWidth(100);
        container.setStyle("-fx-wrap-text: true;");

        timetableGrid.add(container, col, row, 1, rowSpan);
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

        // Update row heights after all sections are added
        Platform.runLater(() -> {
            updateAllRowHeights();
            timetableGrid.requestLayout();
        });
    }

    private void updateAllRowHeights() {
        // Track maximum height needed for each row
        Map<Integer, Double> rowHeights = new HashMap<>();

        // Get all VBox containers in the grid
        timetableGrid.getChildren().stream().filter(node -> node instanceof VBox).forEach(node -> {
            Integer row = GridPane.getRowIndex(node);
            if (row != null && row >= 2) {  // Skip header rows
                VBox vbox = (VBox) node;
                // Calculate total height needed for this container
                double height = vbox.getChildren().size() * 30 + 20;  // 30 per section + padding
                // Update max height for this row if needed
                rowHeights.merge(row, height, Math::max);
            }
        });

        // Apply the maximum heights to row constraints
        rowHeights.forEach((row, height) -> {
            double finalHeight = Math.max(height, 50);  // Ensure minimum height of 50
            if (row < timetableGrid.getRowConstraints().size()) {
                RowConstraints rc = timetableGrid.getRowConstraints().get(row);
                rc.setMinHeight(finalHeight);
                rc.setPrefHeight(finalHeight);
            }
        });
    }

    private void generateMockSections(String subject, String day, int col) {
        List<Integer> timeslots = new ArrayList<>();
        int           maxSlots  = LAB_SUBJECTS.contains(subject) ? LAB_TIMESLOTS.length : THEORY_TIMESLOTS.length;

        // Generate more timeslots with minimum guarantee
        int numTimeslots = MIN_TIMESLOTS + RANDOM.nextInt(MAX_ADDITIONAL_TIMESLOTS + 1);
        numTimeslots = Math.min(numTimeslots, maxSlots); // Don't exceed available slots

        while (timeslots.size() < numTimeslots) {
            int slot = RANDOM.nextInt(maxSlots);
            if (!timeslots.contains(slot)) timeslots.add(slot);
        }

        for (int timeslot : timeslots) {
            // Generate more sections with minimum guarantee
            int numSections = MIN_SECTIONS_PER_SLOT + RANDOM.nextInt(MAX_ADDITIONAL_SECTIONS + 1);

            for (int i = 0; i < numSections; i++) {
                // Use multiple letters for section codes to handle more sections
                String  sectionCode = String.valueOf((char) ('A' + (i / 26))) + (char) ('A' + (i % 26));
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
