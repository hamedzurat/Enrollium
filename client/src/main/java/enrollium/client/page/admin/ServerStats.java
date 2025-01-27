package enrollium.client.page.admin;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Random;
import java.util.stream.IntStream;

public class ServerStats extends BasePage {

    public static final TranslationKey NAME = TranslationKey.ServerStats;

    private final XYChart.Series<Number, Number> cpuSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> ramSeries = new XYChart.Series<>();
    private int time = 1;

    public ServerStats() {
        super();

        addPageHeader(); // Add a header to the page
        setHeaderTitle("Server Status Monitoring"); // Set the header title

        VBox content = new VBox();
        content.setSpacing(20); // Add spacing between elements
        content.setPadding(new Insets(20)); // Add padding around the content

        // Create and configure the CPU and RAM monitoring chart
        StackedAreaChart<Number, Number> serverChart = createServerChart();
        content.getChildren().add(serverChart);

        // Add server status data labels
        Label cpuLabel = new Label("CPU Usage: Monitoring...");
        Label ramLabel = new Label("RAM Usage: Monitoring...");
        content.getChildren().addAll(cpuLabel, ramLabel);

        // Add the content to the main layout
        addNode(content);

        // Simulate server data updates
        simulateServerData(cpuLabel, ramLabel);
    }

    private void setHeaderTitle(String title) {
        // Placeholder method for setting header title
    }

    private StackedAreaChart<Number, Number> createServerChart() {
        var xAxis = new NumberAxis(1, 30, 1);
        xAxis.setLabel("Time (Seconds)");

        var yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Usage (%)");

        cpuSeries.setName("CPU Usage");
        ramSeries.setName("RAM Usage");

        var chart = new StackedAreaChart<>(xAxis, yAxis);
        chart.setTitle("Server CPU and RAM Monitoring");
        chart.setMinHeight(300);
        chart.getData().addAll(cpuSeries, ramSeries);

        return chart;
    }

    private void simulateServerData(Label cpuLabel, Label ramLabel) {
        Random random = new Random();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (time > 30) {
                cpuSeries.getData().remove(0);
                ramSeries.getData().remove(0);
            }

            int cpuUsage = random.nextInt(101); // Random CPU usage between 0-100%
            int ramUsage = random.nextInt(101); // Random RAM usage between 0-100%

            cpuSeries.getData().add(new XYChart.Data<>(time, cpuUsage));
            ramSeries.getData().add(new XYChart.Data<>(time, ramUsage));

            cpuLabel.setText("CPU Usage: " + cpuUsage + "%");
            ramLabel.setText("RAM Usage: " + ramUsage + "%");

            time++;
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
