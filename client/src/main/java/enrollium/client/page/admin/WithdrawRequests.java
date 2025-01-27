package enrollium.client.page.admin;

import atlantafx.base.controls.Card;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class WithdrawRequests extends BasePage {

    public static final TranslationKey NAME = TranslationKey.WithdrawRequests;

    private final TableView<WithdrawRequest> requestTable = new TableView<>();
    private final ObservableList<WithdrawRequest> requestData = FXCollections.observableArrayList();

    public WithdrawRequests() {
        super();

        addPageHeader(); // Add a header to the page
        setHeaderTitle("Withdrawal Requests Management");

        // Create main layout container
        VBox content = new VBox();
        content.setSpacing(20); // Add spacing between sections
        content.setPadding(new Insets(20)); // Add padding around the content
        content.setAlignment(Pos.TOP_CENTER); // Align content to the center

        // Add Withdrawal Requests Table
        Card tableCard = createRequestTableCard();

        // Add content to the layout
        content.getChildren().add(tableCard);

        // Add content to the main layout
        addNode(content);

        // Load sample data
        loadRequestData();
    }

    private void setHeaderTitle(String title) {
        // Placeholder for setting header title
    }

    /**
     * Creates a card containing the table of withdrawal requests with action buttons.
     *
     * @return Card containing the withdrawal requests table
     */
    private Card createRequestTableCard() {
        Card tableCard = new Card();
        tableCard.setMinWidth(700); // Adjust the card width

        VBox tableBox = new VBox();
        tableBox.setSpacing(10); // Space between elements
        tableBox.setPadding(new Insets(15)); // Add padding inside the card
        tableBox.setAlignment(Pos.CENTER); // Center align content

        Label tableHeader = new Label("Withdrawal Requests");
        tableHeader.setFont(javafx.scene.text.Font.font(16)); // Set font size for headers

        // Configure columns
        TableColumn<WithdrawRequest, String> studentColumn = new TableColumn<>("Student Name");
        studentColumn.setCellValueFactory(data -> data.getValue().studentNameProperty());

        TableColumn<WithdrawRequest, String> courseColumn = new TableColumn<>("Course Title");
        courseColumn.setCellValueFactory(data -> data.getValue().courseTitleProperty());

        TableColumn<WithdrawRequest, String> reasonColumn = new TableColumn<>("Reason");
        reasonColumn.setCellValueFactory(data -> data.getValue().reasonProperty());

        TableColumn<WithdrawRequest, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        TableColumn<WithdrawRequest, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");
            private final Button rejectButton = new Button("Reject");

            {
                acceptButton.setOnAction(event -> handleAcceptRequest(getTableRow().getItem()));
                rejectButton.setOnAction(event -> handleRejectRequest(getTableRow().getItem()));

                HBox actionBox = new HBox(10, acceptButton, rejectButton);
                actionBox.setAlignment(Pos.CENTER);
                setGraphic(actionBox);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    WithdrawRequest request = getTableRow().getItem();
                    if (request.getStatus().equals("Approved") || request.getStatus().equals("Rejected")) {
                        acceptButton.setDisable(true);
                        rejectButton.setDisable(true);
                    } else {
                        acceptButton.setDisable(false);
                        rejectButton.setDisable(false);
                    }
                }
            }
        });

        // Add columns to the table
        requestTable.getColumns().addAll(studentColumn, courseColumn, reasonColumn, statusColumn, actionColumn);

        // Configure table
        requestTable.setItems(requestData);
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add table to layout
        tableBox.getChildren().addAll(tableHeader, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        tableCard.setBody(tableBox);

        return tableCard;
    }

    /**
     * Loads sample data into the withdrawal requests table.
     */
    private void loadRequestData() {
        requestData.add(new WithdrawRequest("Alice Johnson", "English–I", "Schedule Conflict", "Pending"));
        requestData.add(new WithdrawRequest("Bob Smith", "Discrete Mathematics", "Personal Reasons", "Approved"));
        requestData.add(new WithdrawRequest("Catherine Lee", "Physics", "Course Too Difficult", "Rejected"));
        requestData.add(new WithdrawRequest("David Miller", "Data Structures", "Health Issues", "Pending"));
        requestData.add(new WithdrawRequest("Emily Davis", "Object Oriented Programming", "Workload Issues", "Approved"));
    }

    /**
     * Handles the action to accept a withdrawal request.
     *
     * @param request The withdrawal request to accept
     */
    private void handleAcceptRequest(WithdrawRequest request) {
        if (request != null) {
            request.setStatus("Approved");
            requestTable.refresh();
            showAlert("Request Accepted", "The withdrawal request for " + request.getCourseTitle() + " has been approved.");
        }
    }

    /**
     * Handles the action to reject a withdrawal request.
     *
     * @param request The withdrawal request to reject
     */
    private void handleRejectRequest(WithdrawRequest request) {
        if (request != null) {
            request.setStatus("Rejected");
            requestTable.refresh();
            showAlert("Request Rejected", "The withdrawal request for " + request.getCourseTitle() + " has been rejected.");
        }
    }

    /**
     * Shows an alert dialog.
     *
     * @param title   The title of the alert
     * @param message The message of the alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    /**
     * Model class for a withdrawal request.
     */
    public static class WithdrawRequest {
        private final StringProperty studentName;
        private final StringProperty courseTitle;
        private final StringProperty reason;
        private final StringProperty status;

        public WithdrawRequest(String studentName, String courseTitle, String reason, String status) {
            this.studentName = new SimpleStringProperty(studentName);
            this.courseTitle = new SimpleStringProperty(courseTitle);
            this.reason = new SimpleStringProperty(reason);
            this.status = new SimpleStringProperty(status);
        }

        public StringProperty studentNameProperty() {
            return studentName;
        }

        public StringProperty courseTitleProperty() {
            return courseTitle;
        }

        public StringProperty reasonProperty() {
            return reason;
        }

        public StringProperty statusProperty() {
            return status;
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        public String getStatus() {
            return status.get();
        }

        public String getCourseTitle() {
            return courseTitle.get();
        }
    }
}
