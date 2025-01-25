package enrollium.client.page.students;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enrollium.client.page.BasePage;
import enrollium.client.page.NotificationType;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.memory.Volatile;
import enrollium.rpc.client.ClientRPC;
import enrollium.rpc.core.JsonUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class CourseSchedulePage extends BasePage {
    public static final  TranslationKey        NAME                = TranslationKey.SectionSelection;
    private static final String[]              THEORY_TIMESLOTS    = {"8:30\n -\n9:50", "9:51\n -\n11:10", "11:11\n  -\n12:30", "12:31\n  -\n13:50", "13:51\n  -\n15:10", "15:11\n  -\n16:30"};
    private static final String[]              LAB_TIMESLOTS       = {"8:30\n -\n11:10", "11:11\n -\n13:50", "13:51\n -\n16:30"};
    private static final Map<Integer, Integer> LAB_TO_THEORY_SLOTS = Map.of(0, 0,  // First lab slot starts at first theory slot
            1, 2,  // Second lab slot starts at third theory slot
            2, 4   // Third lab slot starts at fifth theory slot
    );
    private final        GridPane              timetableGrid       = new GridPane();
    private final        Map<String, Color>    subjectColors       = new HashMap<>();
    private final        CompositeDisposable   disposables         = new CompositeDisposable();
    private              Message               statusMessage;
    private              JsonNode              currentScheduleData;
    private              int                   currentCol          = 1;

    public CourseSchedulePage() {
        addPageHeader();
        setupStatusMessage();
        addNode(setupTimetable());
        startDataLoading();

        // Layout updates when scene is ready
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    timetableGrid.requestLayout();
                    timetableGrid.applyCss();
                });
            } else {
                // Cleanup when scene is removed
                disposables.dispose();
            }
        });
    }

    private void setupStatusMessage() {
        statusMessage = new Message("Loading", "Fetching schedule data...", new FontIcon(Material2OutlinedAL.CLOUD_DOWNLOAD));
        statusMessage.getStyleClass().add(Styles.ACCENT);
        addNode(statusMessage);
    }

    private void startDataLoading() {
        String userId = (String) Volatile.getInstance().get("auth_user_id");
        if (userId == null) {
            showError("User not authenticated");
            return;
        }

        // Initial load
        loadScheduleData();

        // Setup periodic refresh
        disposables.add(Observable.interval(8, TimeUnit.SECONDS)
                                  .observeOn(Schedulers.io())
                                  .subscribe(tick -> loadScheduleData(), error -> Platform.runLater(() -> showError("Auto-refresh failed: " + error.getMessage()))));
    }

    private void loadScheduleData() {
        String userId = (String) Volatile.getInstance().get("auth_user_id");

        ClientRPC.getInstance()
                 .call("Course.getSchedule", JsonUtils.createObject().put("userId", userId))
                 .subscribeOn(Schedulers.io())
                 .subscribe(response -> {
                     JsonNode params = response.getParams();
                     if (params.has("error")) {
                         Platform.runLater(() -> showError(params.get("error").asText()));
                         return;
                     }

                     // Check if data actually changed before updating
                     if (!params.equals(currentScheduleData)) {
                         currentScheduleData = params;
                         Platform.runLater(() -> updateScheduleDisplay(params));
                     }
                 }, error -> Platform.runLater(() -> showError("Failed to load schedule: " + error.getMessage())));
    }

    private void updateScheduleDisplay(JsonNode scheduleData) {
        timetableGrid.getChildren().clear();
        currentCol = 1;
        subjectColors.clear();

        setupTimeColumn();

        // Check if there are any subjects
        JsonNode subjectsNode = scheduleData.get("subjects");
        if (subjectsNode == null || subjectsNode.isEmpty()) {
            showSuccess("No courses found for section selection");
            return;
        }

        // Group subjects by type
        Map<String, List<JsonNode>> subjectsByType = new HashMap<>();
        subjectsNode.forEach(subject -> {
            String type = subject.get("subjectType").asText();
            subjectsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(subject);
        });

        // Add day columns for each type
        subjectsNode.forEach(subject -> {
            String   type     = subject.get("subjectType").asText();
            JsonNode daysNode = subject.get("days");
            if (daysNode != null && !daysNode.isEmpty()) {
                if (type.equals("LAB")) {
                    daysNode.forEach(day -> addLabDay(day.get("day").asText(), subject));
                } else {
                    daysNode.forEach(day -> {
                        String dayText = day.get("day").asText();
                        if (dayText.contains("+")) {
                            addTheoryDay(dayText, subject);
                        }
                    });
                }
            }
        });

        showSuccess("Schedule loaded successfully");
        timetableGrid.requestLayout();
    }

    private void handleSectionClick(JsonNode sectionData, JsonNode courseData) {
        boolean isRegistered = sectionData.get("isRegistered").asBoolean();
        String  action       = isRegistered ? "deregister from" : "register for";
        String message = String.format("Attempting to %s section %s", action, sectionData.get("sectionCode").asText());

        showNotification(message, NotificationType.INFO);

        ObjectNode requestParams = JsonUtils.createObject().put("courseId", courseData.get("courseId").asText());

        if (!isRegistered) {
            requestParams.put("sectionId", sectionData.get("sectionId").asText());
        }

        ClientRPC.getInstance()
                 .call("Course.updateRegistration", requestParams)
                 .subscribeOn(Schedulers.io())
                 .subscribe(response -> {
                     if (response.getParams().get("success").asBoolean()) {
                         Platform.runLater(() -> {
                             loadScheduleData(); // Refresh data
                             showNotification("Section update successful", NotificationType.SUCCESS);
                         });
                     } else {
                         Platform.runLater(() -> showNotification("Failed to update section", NotificationType.WARNING));
                     }
                 }, error -> Platform.runLater(() -> showNotification("Error: " + error.getMessage(), NotificationType.WARNING)));
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

        timetableGrid.setStyle("-fx-grid-lines-visible: true; -fx-border-color: black;");

        // Configure row constraints
        // Header rows
        for (int i = 0; i < 2; i++) {
            RowConstraints headerRC = new RowConstraints();
            headerRC.setMinHeight(60);
            headerRC.setPrefHeight(60);
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

        ScrollPane scrollPane = new ScrollPane(timetableGrid);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinViewportWidth(800);
        scrollPane.setMinWidth(800);

        timetableGrid.requestLayout();
        timetableGrid.autosize();

        timetableGrid.applyCss();
        timetableGrid.requestLayout();

        setupColumnConstraints();

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

    private void addLabDay(String day, JsonNode subject) {
        Label dayHeader = new Label(day);
        styleHeader(dayHeader, Styles.BG_ACCENT_SUBTLE);

        timetableGrid.add(dayHeader, currentCol, 0, 1, 1);
        addSubjectColumn(subject.get("subjectName").asText(), currentCol);

        // Add sections from the data
        JsonNode dayData = subject.get("days").elements().next(); // Get first day data
        if (dayData.has("sections")) {
            for (JsonNode section : dayData.get("sections")) {
                addSection(section, subject, currentCol);
            }
        }

        ColumnConstraints cc = new ColumnConstraints();
        cc.setMinWidth(100);
        cc.setPrefWidth(100);
        cc.setMaxWidth(150);
        cc.setHgrow(Priority.NEVER);
        timetableGrid.getColumnConstraints().add(cc);

        currentCol++;

        setupColumnConstraints();
    }

    private void addTheoryDay(String day, JsonNode subject) {
        Label dayHeader = new Label(day);
        styleHeader(dayHeader, Styles.BG_DANGER_SUBTLE);

        timetableGrid.add(dayHeader, currentCol, 0, 1, 1);
        addSubjectColumn(subject.get("subjectName").asText(), currentCol);

        // Add sections from the data
        JsonNode dayData = subject.get("days").elements().next();
        if (dayData.has("sections")) {
            for (JsonNode section : dayData.get("sections")) {
                addSection(section, subject, currentCol);
            }
        }

        ColumnConstraints cc = new ColumnConstraints();
        cc.setMinWidth(100);
        cc.setPrefWidth(100);
        cc.setMaxWidth(150);
        cc.setHgrow(Priority.NEVER);
        timetableGrid.getColumnConstraints().add(cc);

        currentCol++;

        setupColumnConstraints();
    }

    private void addSubjectColumn(String subject, int col) {
        Label subjectHeader = new Label(subject);
        styleSubjectHeader(subjectHeader);
        subjectHeader.setWrapText(true);
        subjectHeader.setPrefHeight(100);
        timetableGrid.add(subjectHeader, col, 1);
    }

    private void setupColumnConstraints() {
        timetableGrid.getColumnConstraints().clear(); // Clear existing constraints

        // Set constraints for all current columns in the grid
        int totalColumns = timetableGrid.getColumnCount();

        for (int i = 0; i < totalColumns; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.ALWAYS);  // Allow equal growth for all columns
            colConstraints.setFillWidth(true);         // Fill available width
            colConstraints.setMinWidth(150);           // Set minimum width to avoid shrinking
            colConstraints.setPrefWidth(150);          // Set preferred width for better appearance
            timetableGrid.getColumnConstraints().add(colConstraints);
        }
    }

    private void addSection(JsonNode section, JsonNode subject, int col) {
        boolean isLab    = subject.get("subjectType").asText().equals("LAB");
        int     timeSlot = section.get("timeSlot").asInt() - 1; // Convert 1-based to 0-based index

        int rowIndex;
        int rowSpan;

        if (isLab) {
            rowIndex = LAB_TO_THEORY_SLOTS.get(timeSlot) + 2;
            rowSpan  = 2;
        } else {
            rowIndex = timeSlot + 2;
            rowSpan  = 1;
        }

        VBox container = getOrCreateCellContainer(col, rowIndex, rowSpan);

        // Create section label with capacity
        String sectionText = String.format("%s (%d/%d)", section.get("sectionCode")
                                                                .asText(), section.get("currentCapacity")
                                                                                  .asInt(), section.get("maxCapacity")
                                                                                                   .asInt());

        Label sectionLabel = new Label(sectionText);
        Color color        = getSubjectColor(subject.get("subjectName").asText());

        // Highlight if registered
        if (section.get("isRegistered").asBoolean()) {
            color = color.deriveColor(0, 1.2, 1, 0.7); // Make it more visible
            sectionLabel.getStyleClass().add(Styles.TEXT_BOLD);
        }

        sectionLabel.setStyle("-fx-background-color: " + toRgbaString(color) + ";" + "-fx-padding: 5;" + "-fx-background-radius: 3;");
        sectionLabel.setMaxWidth(Double.MAX_VALUE);
        sectionLabel.setAlignment(Pos.CENTER);

        // Click handler
        sectionLabel.setOnMouseClicked(e -> handleSectionClick(section, subject));

        container.getChildren().add(sectionLabel);

        // Update row heights after adding section
        Platform.runLater(() -> updateRowHeight(rowIndex));
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

    private void updateRowHeight(int row) {
        // Track maximum height needed for the row
        double maxHeight = 50; // Minimum height

        // Find all VBoxes in this row
        for (Node node : timetableGrid.getChildren()) {
            if (node instanceof VBox vbox && GridPane.getRowIndex(node) == row) {
                double height = vbox.getChildren().size() * 30 + 20; // 30 per section + padding
                maxHeight = Math.max(maxHeight, height);
            }
        }

        // Update row constraint
        if (row < timetableGrid.getRowConstraints().size()) {
            RowConstraints rc = timetableGrid.getRowConstraints().get(row);
            rc.setMinHeight(maxHeight);
            rc.setPrefHeight(maxHeight);
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
        double hue = Math.random() * 360;
        return Color.hsb(hue, 0.3, 0.9, 0.5);
    }

    private String toRgbaString(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255), color.getOpacity());
    }

    private void showError(String message) {
        Message newMessage = new Message("Error", message, new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
        newMessage.getStyleClass().add(Styles.DANGER);
        replaceStatusMessage(newMessage);
    }

    private void showSuccess(String message) {
        Message newMessage = new Message("Success", message, new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE));
        newMessage.getStyleClass().add(Styles.SUCCESS);
        replaceStatusMessage(newMessage);
    }

    private void replaceStatusMessage(Message newMessage) {
        if (statusMessage != null) {
            VBox parent = (VBox) statusMessage.getParent();
            if (parent != null) {
                int index = parent.getChildren().indexOf(statusMessage);
                parent.getChildren().set(index, newMessage);
            }
        }
        statusMessage = newMessage;
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
