package enrollium.client;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import static java.lang.Double.MAX_VALUE;


@Slf4j
// Implements Thread.UncaughtExceptionHandler to globally handle any unhandled exceptions in the application.
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Stage stage;

    public DefaultExceptionHandler(Stage stage) {
        // Stores a reference to the main application window (Stage) after later uses.
        this.stage = stage;
    }

    // uncaughtException is triggered when an uncaught exception occurs.
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("{}", e.getMessage(), e);

        // creates a user-friendly error dialog.
        var dialog = createExceptionDialog(e);
        // pauses the program to show the dialog.
        if (dialog != null) dialog.showAndWait();
    }

    private Alert createExceptionDialog(Throwable error) {
        Objects.requireNonNull(error);

        // Create alert
        var alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(error.getMessage());

        // Show stack strace
        try (var sw = new StringWriter(); var printWriter = new PrintWriter(sw)) {
            error.printStackTrace(printWriter);

            var label = new Label("Full stacktrace:");

            var textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setMaxWidth(MAX_VALUE);
            textArea.setMaxHeight(MAX_VALUE);

            var content = new VBox(5, label, textArea);
            content.setMaxWidth(MAX_VALUE);

            alert.getDialogPane().setExpandableContent(content);
            alert.initOwner(stage);

            return alert;
        } catch (IOException e) {
            log.error("{}", e.getMessage(), e);
            return null;
        }
    }
}
