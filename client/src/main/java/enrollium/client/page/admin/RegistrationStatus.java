package enrollium.client.page.admin;

import atlantafx.base.controls.Card;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class RegistrationStatus extends BasePage {

    public static final TranslationKey NAME = TranslationKey.RegistrationStatus;

    public RegistrationStatus() {
        super();

        addPageHeader(); // Add a header to the page
        setHeaderTitle("Registration Status Overview"); // Set the header title

        // Create main layout container
        VBox content = new VBox();
        content.setSpacing(20); // Space between components
        content.setPadding(new Insets(20)); // Add padding around the content
        content.setAlignment(Pos.TOP_CENTER); // Align content to the center

        // Add Registration Progress section
        Card registrationProgressCard = createRegistrationProgressCard();

        // Add Current Stats section
        Card currentStatsCard = createCurrentStatsCard();

        // Add Pie Chart section
        Card pieChartCard = createPieChartCard();

        // Add content to the layout
        content.getChildren().addAll(registrationProgressCard, currentStatsCard, pieChartCard);

        // Add content to the main layout
        addNode(content);
    }

    private void setHeaderTitle(String title) {
        // Placeholder method for setting header title
    }

    /**
     * Creates a card displaying registration progress.
     *
     * @return Card displaying registration progress
     */
    private Card createRegistrationProgressCard() {
        Card progressCard = new Card();
        progressCard.setMinWidth(400); // Adjust the card width

        VBox progressBox = new VBox();
        progressBox.setSpacing(10); // Space between elements
        progressBox.setPadding(new Insets(15)); // Add padding inside the card
        progressBox.setAlignment(Pos.CENTER_LEFT); // Align content to the left

        Label progressHeader = new Label("Overall Registration Progress");
        progressHeader.setFont(javafx.scene.text.Font.font(16)); // Set font size for headers

        javafx.scene.control.ProgressBar registrationProgressBar = new javafx.scene.control.ProgressBar();
        registrationProgressBar.setProgress(0.75); // Example: 75% progress
        registrationProgressBar.setPrefWidth(350);

        Label progressLabel = new Label("75% of students have completed registration");

        progressBox.getChildren().addAll(progressHeader, registrationProgressBar, progressLabel);
        progressCard.setBody(progressBox);

        return progressCard;
    }

    /**
     * Creates a card displaying current registration statistics.
     *
     * @return Card displaying current statistics
     */
    private Card createCurrentStatsCard() {
        Card statsCard = new Card();
        statsCard.setMinWidth(400); // Adjust the card width

        VBox statsBox = new VBox();
        statsBox.setSpacing(10); // Space between elements
        statsBox.setPadding(new Insets(15)); // Add padding inside the card
        statsBox.setAlignment(Pos.CENTER_LEFT); // Align content to the left

        Label statsHeader = new Label("Current Registration Statistics");
        statsHeader.setFont(javafx.scene.text.Font.font(16)); // Set font size for headers

        statsBox.getChildren().addAll(
                createStatItem("Total Students:", "1,200"),
                createStatItem("Registered Students:", "900"),
                createStatItem("Pending Registrations:", "300")
        );
        statsCard.setBody(statsBox);

        return statsCard;
    }

    /**
     * Creates a card displaying a pie chart for registration distribution.
     *
     * @return Card displaying a pie chart
     */
    private Card createPieChartCard() {
        Card pieCard = new Card();
        pieCard.setMinWidth(400); // Adjust the card width

        VBox pieBox = new VBox();
        pieBox.setSpacing(10); // Space between elements
        pieBox.setPadding(new Insets(15)); // Add padding inside the card
        pieBox.setAlignment(Pos.CENTER); // Center align content

        Label pieHeader = new Label("Registration Distribution");
        pieHeader.setFont(javafx.scene.text.Font.font(16)); // Set font size for headers

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Registered Students", 900),
                new PieChart.Data("Pending Registrations", 300)
        );

        PieChart pieChart = new PieChart(pieChartData);
//        pieChart.setTitle("Registration Breakdown");
        pieChart.setPrefSize(350, 350); // Set chart size

        pieBox.getChildren().addAll(pieHeader, pieChart);
        pieCard.setBody(pieBox);

        return pieCard;
    }

    /**
     * Creates a row for displaying a key-value pair in the statistics card.
     *
     * @param label The label for the statistic
     * @param value The value for the statistic
     * @return VBox containing the key-value pair
     */
    private VBox createStatItem(String label, String value) {
        Label keyLabel = new Label(label);
        keyLabel.setFont(javafx.scene.text.Font.font(14));

        Label valueLabel = new Label(value);
        valueLabel.setFont(javafx.scene.text.Font.font(14));

        VBox statItem = new VBox(5, keyLabel, valueLabel);
        statItem.setAlignment(Pos.CENTER_LEFT);
        return statItem;
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
