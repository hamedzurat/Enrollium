package enrollium.client.page.students;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.memory.Volatile;
import enrollium.rpc.client.ClientRPC;
import enrollium.rpc.core.JsonUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;


public class TradeSection extends BasePage {
    public static final TranslationKey            NAME                 = TranslationKey.TradeSection;
    private final       ClientRPC                 rpc                  = ClientRPC.getInstance();
    private final       String                    studentId;
    private final       ComboBox<SectionItem>     mySectionComboBox    = new ComboBox<>();
    private final       ComboBox<SectionItem>     offerSectionComboBox = new ComboBox<>();
    private final       TextField                 partnerSectionField  = new TextField();
    private final       TextArea                  noteField            = new TextArea();
    private final       TableView<TradeItem>      tradesTable          = new TableView<>();
    private final       ObservableList<TradeItem> tradesList           = FXCollections.observableArrayList();
    private final       Volatile                  memory               = Volatile.getInstance();
    private             Message                   statusMessage;

    public TradeSection() {
        super();

        this.studentId     = (String) memory.get("auth_user_id");
        this.statusMessage = new Message("Loading...", "wait a bit :)", new FontIcon(Material2OutlinedAL.INFO));

        if (memory.get("auth_user_id") == null || memory.get("auth_user_type") == null) {
            Message msg = new Message("Error", "User information not found. Please log first.", new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
            msg.getStyleClass().add(Styles.DANGER);
            replaceStatusMessage(msg, Styles.WARNING);
        } else {
            setupUI();
            loadData();
        }
    }

    private void setupUI() {
        addPageHeader();
        addNode(statusMessage);

        // Create sections
        VBox swapSection        = createSwapSection();
        VBox tradeSection       = createTradeSection();
        VBox tradesTableSection = createTradesTableSection();

        // Add sections to page
        addSection("Direct Swap", swapSection);
        addSection("Offer Trade", tradeSection);
        addSection("Available Trades", tradesTableSection);
    }

    private VBox createSwapSection() {
        Label selectLabel = new Label("Select your section to swap:");
        mySectionComboBox.setMaxWidth(Double.MAX_VALUE);

        Label partnerLabel = new Label("Partner's Section ID:");
        partnerSectionField.setPromptText("Enter section ID");

        Button swapButton = new Button("Request Swap");
        swapButton.setOnAction(_ -> handleSwapRequest());

        VBox section = new VBox(10);
        section.getChildren().addAll(selectLabel, mySectionComboBox, partnerLabel, partnerSectionField, swapButton);
        section.setPadding(new Insets(20));
        return section;
    }

    private VBox createTradeSection() {
        Label selectLabel = new Label("Select section to offer:");
        offerSectionComboBox.setMaxWidth(Double.MAX_VALUE);

        Label noteLabel = new Label("Note (optional):");
        noteField.setPromptText("Enter any preferences or requirements");
        noteField.setPrefRowCount(3);

        Button offerButton = new Button("Post Trade Offer");
        offerButton.setOnAction(_ -> handleTradeOffer());

        VBox section = new VBox(10);
        section.getChildren().addAll(selectLabel, offerSectionComboBox, noteLabel, noteField, offerButton);
        section.setPadding(new Insets(20));
        return section;
    }

    private VBox createTradesTableSection() {
        // Setup table columns
        TableColumn<TradeItem, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> data.getValue().courseNameProperty());

        TableColumn<TradeItem, String> sectionCol = new TableColumn<>("Section");
        sectionCol.setCellValueFactory(data -> data.getValue().sectionNameProperty());

        TableColumn<TradeItem, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(data -> data.getValue().noteProperty());

        TableColumn<TradeItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());

        // Add action column
        TableColumn<TradeItem, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(_ -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");

            {
                acceptButton.setOnAction(e -> {
                    TradeItem item = getTableView().getItems().get(getIndex());
                    handleAcceptTrade(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    TradeItem trade = getTableView().getItems().get(getIndex());
                    setGraphic(trade.getStatus().equals("PENDING") ? acceptButton : null);
                }
            }
        });

        tradesTable.getColumns().addAll(courseCol, sectionCol, noteCol, statusCol, actionCol);
        tradesTable.setItems(tradesList);

        VBox section = new VBox(10);
        section.getChildren().add(tradesTable);
        section.setPadding(new Insets(20));
        VBox.setVgrow(tradesTable, Priority.ALWAYS);

        return section;
    }

    private void loadData() {
        // Load student's registered sections
        rpc.call("Trade.getRegisteredSections", createParams("studentId", studentId)).subscribe(response -> {
            Platform.runLater(() -> {
                JsonNode items = response.getParams().get("items");
                if (items == null || items.isEmpty()) {
                    showError("No Sections", "You don't have any registered sections to trade with");
                    mySectionComboBox.setDisable(true);
                    offerSectionComboBox.setDisable(true);
                    partnerSectionField.setDisable(true);
                    noteField.setDisable(true);
                    return;
                }

                items.forEach(item -> {
                    SectionItem sectionItem = new SectionItem(item.get("id").asText(), item.get("name")
                                                                                           .asText(), item.get("courseName")
                                                                                                          .asText());
                    mySectionComboBox.getItems().add(sectionItem);
                    offerSectionComboBox.getItems().add(sectionItem);
                });
            });
        }, error -> showError("Failed to load sections", String.valueOf(error)));

        // Load available trades
        loadAvailableTrades();
    }

    private void loadAvailableTrades() {
        rpc.call("Trade.listAvailableTrades", createParams("studentId", studentId))
           .subscribe(response -> {
               Platform.runLater(() -> {
                   tradesList.clear();
                   JsonNode items = response.getParams().get("items");

                   if (items == null || items.isEmpty()) {
                       return;
                   }

                   items.forEach(item -> {
                       // Skip trades offered by current user
                       String offeredBy = item.get("offeredBy").asText();
                       if (!offeredBy.equals(studentId)) {
                           TradeItem tradeItem = new TradeItem(
                                   item.get("id").asText(),
                                   item.get("courseName").asText(),
                                   item.get("currentSectionName").asText(),
                                   item.get("note").asText(),
                                   item.get("status").asText(),
                                   offeredBy
                           );
                           tradesList.add(tradeItem);
                       }
                   });

                   // Add "My Trades" method to see your own trades
                   loadMyTrades();
               });
           }, error -> showError("Failed to load trades", String.valueOf(error)));
    }

    private void loadMyTrades() {
        rpc.call("Trade.listMyTrades", createParams("studentId", studentId))
           .subscribe(response -> {
               Platform.runLater(() -> {
                   JsonNode items = response.getParams().get("items");

                   if (items == null || items.isEmpty()) {
                       return;
                   }

                   items.forEach(item -> {
                       TradeItem tradeItem = new TradeItem(
                               item.get("id").asText(),
                               item.get("courseName").asText(),
                               item.get("currentSectionName").asText(),
                               item.get("note").asText(),
                               item.get("status").asText(),
                               item.get("offeredBy").asText()
                       );
                       tradesList.add(tradeItem);
                   });
               });
           }, error -> showError("Failed to load my trades", String.valueOf(error)));
    }

    private void handleSwapRequest() {
        SectionItem selectedSection = mySectionComboBox.getValue();
        String      partnerSection  = partnerSectionField.getText().trim();

        if (selectedSection == null || partnerSection.isEmpty()) {
            showError("Missing Information", "Please select your section and enter partner's section ID");
            return;
        }

        JsonNode params = createParams("studentId", studentId, "sectionId", selectedSection.getId(), "note", "Direct swap request", "desiredSectionId", partnerSection);

        rpc.call("Trade.offerTrade", JsonUtils.toJson(params)).subscribe(response -> {
            Platform.runLater(() -> {
                showSuccess("Swap Request Sent", "Your swap request has been submitted successfully");
                partnerSectionField.clear();
                loadAvailableTrades();
            });
        }, error -> showError("Failed to send swap request", String.valueOf(error)));
    }

    private void handleTradeOffer() {
        SectionItem selectedSection = offerSectionComboBox.getValue();
        String      note            = noteField.getText().trim();

        if (selectedSection == null) {
            showError("Missing Information", "Please select a section to offer");
            return;
        }

        JsonNode params = createParams("studentId", studentId, "sectionId", selectedSection.getId(), "note", note);

        rpc.call("Trade.offerTrade", JsonUtils.toJson(params)).subscribe(response -> {
            Platform.runLater(() -> {
                showSuccess("Trade Offered", "Your trade offer has been posted successfully");
                noteField.clear();
                loadAvailableTrades();
            });
        }, error -> showError("Failed to post trade offer", String.valueOf(error)));
    }

    private void handleAcceptTrade(TradeItem trade) {
        SectionItem mySection = mySectionComboBox.getValue();
        if (mySection == null) {
            showError("Section Required", "Please select your section to trade");
            return;
        }

        JsonNode params = createParams("tradeId", trade.getId(), "studentId", studentId, "swapSectionId", mySection.getId());

        rpc.call("Trade.acceptTrade", JsonUtils.toJson(params)).subscribe(response -> {
            Platform.runLater(() -> {
                showSuccess("Trade Accepted", "Trade has been accepted successfully");
                loadAvailableTrades();
            });
        }, error -> showError("Failed to accept trade", String.valueOf(error)));
    }

    private JsonNode createParams(Object... keyValues) {
        ObjectNode params = JsonUtils.createObject();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object value = keyValues[i + 1];
            String key   = keyValues[i].toString();

            if (value == null) {
                params.putNull(key);
            } else {
                params.put(key, value.toString());
            }
        }
        return params;
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private void showError(String title, String message) {
        Message newMessage = new Message(title, message, new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
        replaceStatusMessage(newMessage, Styles.DANGER);
//        showNotification(message, NotificationType.WARNING);
    }

    private void showSuccess(String title, String message) {
        Message newMessage = new Message(title, message, new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE));
        replaceStatusMessage(newMessage, Styles.SUCCESS);
    }

    private void replaceStatusMessage(Message newMessage, String style) {
        VBox parent = (VBox) statusMessage.getParent();
        int  index  = parent.getChildren().indexOf(statusMessage);
        newMessage.getStyleClass().add(style);
        parent.getChildren().set(index, newMessage);
        statusMessage = newMessage;
    }
}


// Helper class for section items in combo boxes
class SectionItem {
    private final String id;
    private final String name;
    private final String courseName;

    public SectionItem(String id, String name, String courseName) {
        this.id         = id;
        this.name       = name;
        this.courseName = courseName;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return courseName + " - " + name;
    }
}


// Helper class for trade items in table
class TradeItem {
    private final String               id;
    private final SimpleStringProperty courseName;
    private final SimpleStringProperty sectionName;
    private final SimpleStringProperty note;
    private final SimpleStringProperty status;
    private final String               offeredBy;

    public TradeItem(String id, String courseName, String sectionName, String note, String status, String offeredBy) {
        this.id          = id;
        this.courseName  = new SimpleStringProperty(courseName);
        this.sectionName = new SimpleStringProperty(sectionName);
        this.note        = new SimpleStringProperty(note);
        this.status      = new SimpleStringProperty(status);
        this.offeredBy   = offeredBy;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty courseNameProperty() {
        return courseName;
    }

    public SimpleStringProperty sectionNameProperty() {
        return sectionName;
    }

    public SimpleStringProperty noteProperty() {
        return note;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }
}
