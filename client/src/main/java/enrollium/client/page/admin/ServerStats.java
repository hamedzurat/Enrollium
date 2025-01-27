package enrollium.client.page.admin;

import com.fasterxml.jackson.databind.JsonNode;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.rpc.client.ClientRPC;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


public class ServerStats extends BasePage {
    public static final TranslationKey                 NAME          = TranslationKey.ServerStats;
    private final       XYChart.Series<Number, Number> cpuSeries     = new XYChart.Series<>();
    private final       XYChart.Series<Number, Number> ramSeries     = new XYChart.Series<>();
    private final       XYChart.Series<Number, Number> diskSeries    = new XYChart.Series<>();
    private final       XYChart.Series<Number, Number> networkSeries = new XYChart.Series<>();
    private             int                            time          = 1;
    private             Timeline                       timeline;

    public ServerStats() {
        super();

        addPageHeader();

        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));

        LineChart<Number, Number> cpuChart     = createChart("CPU Usage (%)", cpuSeries);
        LineChart<Number, Number> ramChart     = createChart("RAM Usage (%)", ramSeries);
        LineChart<Number, Number> diskChart    = createChart("Disk Usage (%)", diskSeries);
        LineChart<Number, Number> networkChart = createChart("Network Usage (KB)", networkSeries);

        content.getChildren().addAll(cpuChart, ramChart, diskChart, networkChart);

        addNode(content);
        fetchServerStats();

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && timeline != null) {
                timeline.stop();
            }
        });
    }

    private LineChart<Number, Number> createChart(String title, XYChart.Series<Number, Number> series) {
        var xAxis = new NumberAxis(1, 30, 1);
        xAxis.setLabel("Time (Seconds)");

        var yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel(title);

        series.setName(title);

        var chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setMinHeight(250);
        chart.getData().add(series);

        return chart;
    }

    private void fetchServerStats() {
        ClientRPC client = ClientRPC.getInstance();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), _ -> client.call("getServerStats", null)
                                                                             .subscribe(response -> {
                                                                                 if (!response.isError()) {
                                                                                     JsonNode params = response.getParams();

                                                                                     int cpuUsage = params.get("cpu")
                                                                                                          .asInt();
                                                                                     int ramUsage = params.get("ram")
                                                                                                          .asInt();
                                                                                     int diskUsage = params.get("disk")
                                                                                                           .asInt();
                                                                                     int networkUsage = params.get("network")
                                                                                                              .asInt();

                                                                                     if (time > 30) {
                                                                                         cpuSeries.getData()
                                                                                                  .removeFirst();
                                                                                         ramSeries.getData()
                                                                                                  .removeFirst();
                                                                                         diskSeries.getData()
                                                                                                   .removeFirst();
                                                                                         networkSeries.getData()
                                                                                                      .removeFirst();
                                                                                     }

                                                                                     Platform.runLater(() -> {
                                                                                         cpuSeries.getData()
                                                                                                  .add(new XYChart.Data<>(time, cpuUsage));
                                                                                         ramSeries.getData()
                                                                                                  .add(new XYChart.Data<>(time, ramUsage));
                                                                                         diskSeries.getData()
                                                                                                   .add(new XYChart.Data<>(time, diskUsage));
                                                                                         networkSeries.getData()
                                                                                                      .add(new XYChart.Data<>(time, networkUsage));
                                                                                     });

                                                                                     time++;
                                                                                 } else {
                                                                                     System.err.println("Error fetching server stats: " + response.getErrorMessage());
                                                                                 }
                                                                             })));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
