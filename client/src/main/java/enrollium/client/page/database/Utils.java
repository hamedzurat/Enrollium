package enrollium.client.page.database;

import atlantafx.base.theme.Styles;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;


public class Utils {
    public static HBox createActionButtons(Runnable onCreate, Runnable onUpdate, Runnable onDelete) {
        Button createBtn = new Button("Create", new FontIcon(Feather.PLUS));
        createBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SUCCESS);
        createBtn.setMnemonicParsing(true);
        createBtn.setOnAction(e -> onCreate.run());

        Button updateBtn = new Button("Update", new FontIcon(Feather.UPLOAD));
        updateBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        updateBtn.setMnemonicParsing(true);
        updateBtn.setOnAction(e -> onUpdate.run());

        Button deleteBtn = new Button("Delete", new FontIcon(Feather.TRASH));
        deleteBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        deleteBtn.setContentDisplay(ContentDisplay.RIGHT);
        deleteBtn.setMnemonicParsing(true);
        deleteBtn.setOnAction(e -> onDelete.run());

        HBox actions = new HBox(10, createBtn, updateBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);
        return actions;
    }

    public static void styleCourseTableView(TableView<CourseData> tableView) {
        tableView.setStyle("""
                -color-cell-bg-selected: -color-accent-emphasis;
                -color-cell-fg-selected: -color-fg-emphasis;
                -color-cell-bg-selected-focused: -color-accent-emphasis;
                -color-cell-fg-selected-focused: -color-fg-emphasis;""");

        Styles.toggleStyleClass(tableView, Styles.BORDERED);
        Styles.toggleStyleClass(tableView, Styles.STRIPED);
        Styles.toggleStyleClass(tableView, Styles.DENSE);

//        tableView.setMinHeight(100);
//        tableView.setMaxHeight(500);

        adjustTableHeight(tableView);
    }

    public static void adjustTableHeight(TableView<?> tableView) {
        // Add a listener to trigger after items are loaded
        tableView.getItems().addListener((InvalidationListener) change -> {
            Platform.runLater(() -> {
                // Force the TableView to layout and render the rows
                tableView.layout();

                // Try multiple times to ensure rows are rendered
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
                    // Get the header height
                    Node   header       = tableView.lookup("TableHeaderRow");
                    double headerHeight = header == null ? 28 : header.getBoundsInLocal().getHeight();

                    // Look up the first visible row after layout
                    TableRow<?> firstRow = (TableRow<?>) tableView.lookup(".table-row-cell");
                    if (firstRow != null && firstRow.getHeight() > 0) {
                        double rowHeight = firstRow.getHeight();

                        // Calculate the total height
                        int    rowCount    = tableView.getItems().size();
                        double totalHeight = headerHeight + (rowHeight * rowCount) + 2;

                        // Apply the height with a reasonable max limit
                        double maxHeight = 800;
                        tableView.setMinHeight(Math.min(totalHeight, maxHeight));
                        tableView.setMaxHeight(Math.min(totalHeight, maxHeight));
                    }
                }));

                // Retry a few times to make sure rows are rendered
                timeline.setCycleCount(5);
                timeline.play();
            });
        });
    }
}
