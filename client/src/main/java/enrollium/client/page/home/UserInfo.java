package enrollium.client.page.home;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import com.fasterxml.jackson.databind.JsonNode;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.memory.Volatile;
import enrollium.rpc.client.ClientRPC;
import enrollium.rpc.core.JsonUtils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;


public class UserInfo extends BasePage {
    public static final TranslationKey NAME   = TranslationKey.UserInfo;
    private final       Volatile       memory = Volatile.getInstance();
    private             VBox           contentBox;
    private             Message        statusMessage;

    public UserInfo() {
        super();
        addPageHeader();
        setupUI();
        loadUserData();
    }

    private void setupUI() {
        contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20, 0, 0, 0));
        contentBox.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // Status message
        statusMessage = new Message("Loading", "Fetching user information...", new FontIcon(Material2OutlinedAL.CLOUD_DOWNLOAD));
        statusMessage.getStyleClass().add(Styles.ACCENT);

        contentBox.getChildren().addAll(statusMessage);
        addNode(contentBox);
    }

    private void loadUserData() {
        String userId   = (String) memory.get("auth_user_id");
        String userType = (String) memory.get("auth_user_type");

        if (userId == null || userType == null) {
            showError("User information not found. Please log first.");
            return;
        }

        Single<?> dataRequest;
        if ("STUDENT".equalsIgnoreCase(userType)) {
            dataRequest = loadStudentData(userId);
        } else if ("TEACHER".equalsIgnoreCase(userType) || "ADMIN".equalsIgnoreCase(userType)) {
            dataRequest = loadFacultyData(userId);
        } else {
            dataRequest = Single.error(new IllegalStateException("Unknown user type: " + userType));
        }

        dataRequest.subscribeOn(Schedulers.io()).subscribe( //
                _ -> Platform.runLater(() -> showSuccess("Data loaded successfully for " + userId)), //
                error -> Platform.runLater(() -> showError("Failed to load user data: " + error.getMessage())));
    }

    private Single<?> loadStudentData(String userId) {
        return Single.zip( //
                ClientRPC.getInstance()
                         .call("Student.getById", JsonUtils.createObject()
                                                           .put("id", userId)
                                                           .put("limit", 100)
                                                           .put("offset", 0)), //
                ClientRPC.getInstance()
                         .call("Course.getByStudent", JsonUtils.createObject()
                                                               .put("studentId", userId)
                                                               .put("limit", 100)
                                                               .put("offset", 0)), //
                (studentData, courseData) -> {
                    Platform.runLater(() -> displayStudentInfo(studentData.getParams(), courseData.getParams()));
                    return true;
                });
    }

    private Single<?> loadFacultyData(String userId) {
        return Single.zip( //
                ClientRPC.getInstance()
                         .call("Faculty.getById", JsonUtils.createObject()
                                                           .put("id", userId)
                                                           .put("limit", 100)
                                                           .put("offset", 0)), //
                ClientRPC.getInstance()
                         .call("Section.getByTeacher", JsonUtils.createObject().put("teacherId", userId)), //
                (facultyData, sectionData) -> {
                    Platform.runLater(() -> displayFacultyInfo(facultyData.getParams(), sectionData.getParams()));
                    return true;
                });
    }

    private void displayStudentInfo(JsonNode studentData, JsonNode courseData) {
        contentBox.getChildren().clear();
        contentBox.getChildren().add(statusMessage);

        // Basic Info Section
        VBox basicInfo = new VBox(10);
        basicInfo.getChildren().addAll( //
                createInfoHeader("Basic Information", Feather.USER), //
                createInfoRow("Name", studentData.get("name").asText()), //
                createInfoRow("Email", studentData.get("email").asText()), //
                createInfoRow("University ID", studentData.get("universityId").asText()) //
        );

        // Courses Section
        VBox coursesInfo = new VBox(10);
        coursesInfo.getChildren().add(createInfoHeader("Current Courses", Feather.BOOK));

        double totalGrade       = 0;
        int    completedCourses = 0;

        var courses = courseData.get("items");
        if (courses.isEmpty()) {
            Message msg = new Message("Warning", "No Course Found", new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
            msg.getStyleClass().add(Styles.WARNING);
            coursesInfo.getChildren().add(msg);
        } else {
            for (var course : courses) {
                String status      = course.get("status").asText();
                String subjectName = course.get("subjectName").asText();

                String details = switch (status) {
                    case "COMPLETED" -> {
                        double grade = course.get("grade").asDouble();
                        totalGrade += grade;
                        completedCourses++;
                        yield String.format("%s (Grade: %.2f)", subjectName, grade);
                    }
                    case "REGISTERED" -> subjectName + " (Registered)";
                    case "SELECTED" -> subjectName + " (Selected)";
                    default -> subjectName + " (" + status + ")";
                };

                coursesInfo.getChildren().add(createInfoRow("Course", details));
            }
        }

        // CGPA Calculation
        if (completedCourses > 0) {
            double cgpa = totalGrade / completedCourses;
            coursesInfo.getChildren().add(createInfoHeader("CGPA: " + String.format("%.2f", cgpa), Feather.ACTIVITY));
        } else {
            coursesInfo.getChildren().add(createInfoHeader("CGPA: N/A", Feather.ACTIVITY));
        }

        contentBox.getChildren().addAll(basicInfo, coursesInfo);
    }

    private void displayFacultyInfo(JsonNode facultyData, JsonNode sectionData) {
        contentBox.getChildren().clear();
        contentBox.getChildren().add(statusMessage);

        // Basic Info Section
        VBox basicInfo = new VBox(10);
        basicInfo.getChildren()
                 .addAll(createInfoHeader("Basic Information", Feather.USER), createInfoRow("Name", facultyData.get("name")
                                                                                                               .asText()), createInfoRow("Email", facultyData.get("email")
                                                                                                                                                             .asText()), createInfoRow("Shortcode", facultyData.get("shortcode")
                                                                                                                                                                                                               .asText()), createInfoRow("User Type", facultyData.get("type")
                                                                                                                                                                                                                                                                 .asText()));

        // Sections Section
        VBox sectionsInfo = new VBox(10);
        sectionsInfo.getChildren().add(createInfoHeader("Current Sections", Feather.USERS));

        var sections = sectionData.get("items");
        if (sections.isEmpty()) {
            Message msg = new Message("Warning", "No Section Found", new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
            msg.getStyleClass().add(Styles.WARNING);
            sectionsInfo.getChildren().add(msg);
        } else {
            for (var section : sections) {
                VBox sectionBox = new VBox(5);
                String sectionDetails = String.format("[%s] %s (%s) - %d/%d students", section.get("trimesterCode")
                                                                                              .asText(), section.get("subjectName")
                                                                                                                .asText(), section.get("section")
                                                                                                                                  .asText(), section.get("currentCapacity")
                                                                                                                                                    .asInt(), section.get("maxCapacity")
                                                                                                                                                                     .asInt());
                sectionBox.getChildren().add(createInfoRow("Section", sectionDetails));

                // Add space-time information
                JsonNode spaceTimes = section.get("spaceTimes");
                if (spaceTimes != null && spaceTimes.isArray()) {
                    for (JsonNode spaceTime : spaceTimes) {
                        String spaceTimeDetails = String.format("%s - Room: %s, (Timeslot: %d)", spaceTime.get("dayOfWeek")
                                                                                                          .asText()
                                                                                                          .toLowerCase(), spaceTime.get("roomNumber")
                                                                                                                                   .asText(), spaceTime.get("timeSlot")
                                                                                                                                                       .asInt());
                        sectionBox.getChildren().add(createInfoRow("             -", spaceTimeDetails));
                    }
                }

                sectionsInfo.getChildren().add(sectionBox);
            }
        }

        contentBox.getChildren().addAll(basicInfo, sectionsInfo);
    }

    private Label createInfoHeader(String text, Feather icon) {
        Label label = new Label(text, new FontIcon(icon));
        label.getStyleClass().addAll(Styles.TITLE_3, "info-header");
        label.setPadding(new Insets(20, 0, 0, 0));
        return label;
    }

    private Label createInfoRow(String label, String value) {
        Label labelPart = new Label(label + ": ");
        labelPart.getStyleClass().add(Styles.TEXT_BOLD);

        Label valuePart = new Label(value);
        valuePart.setWrapText(true);

        Label infoLabel = new Label();
        infoLabel.setGraphic(new HBox(labelPart, valuePart));
        infoLabel.getStyleClass().add(Styles.TITLE_4);
        return infoLabel;
    }

    private void replaceStatusMessage(Message newMessage, String style) {
        VBox parent = (VBox) statusMessage.getParent();
        int  index  = parent.getChildren().indexOf(statusMessage);
        newMessage.getStyleClass().add(style);
        parent.getChildren().set(index, newMessage);
        statusMessage = newMessage;
    }

    private void showError(String message) {
        Message newMessage = new Message("Error", message, new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
        replaceStatusMessage(newMessage, Styles.DANGER);
//        showNotification(message, NotificationType.WARNING);
    }

    private void showSuccess(String message) {
        Message newMessage = new Message("Success", message, new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE));
        replaceStatusMessage(newMessage, Styles.SUCCESS);
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
