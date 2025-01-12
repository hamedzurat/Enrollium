package enrollium.client;

import enrollium.client.event.BrowseEvent;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.HotkeyEvent;
import enrollium.client.layout.ApplicationWindow;
import enrollium.client.theme.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;


public class Launcher extends Application {
    public static final List<KeyCodeCombination> SUPPORTED_HOTKEYS = List.of(new KeyCodeCombination(KeyCode.SLASH), new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Sets a global exception handler (DefaultExceptionHandler).
        Thread.currentThread().setUncaughtExceptionHandler(new DefaultExceptionHandler(stage));
        // Loads application configuration from application.properties.
        loadApplicationProperties();

        // Creates the main window with size
        var scene = new Scene(new ApplicationWindow(), ApplicationWindow.MIN_WIDTH + 80, 768);
        // Binds key press events to dispatchHotkeys() for handling shortcuts.
        scene.setOnKeyPressed(event -> {
            for (KeyCodeCombination k : SUPPORTED_HOTKEYS) {
                if (k.match(event)) {
                    DefaultEventBus.getInstance().publish(new HotkeyEvent(k));
                    return;
                }
            }
        });

        // Initializes the ThemeManager to apply the default theme.
        var tm = ThemeManager.getInstance();
        tm.setScene(scene);
        tm.setTheme(tm.getDefaultTheme());

        // Loads the CSS stylesheet for GUI styling.
        scene.getStylesheets().addAll(Resources.resolve("assets/styles/index.css"));

        // Basic conf
        stage.setScene(scene);
        stage.setTitle(System.getProperty("app.name"));
        stage.setResizable(true);
        stage.setOnCloseRequest(_ -> Platform.exit());

        // Loads application icons of different sizes for cross-platform compatibility.
        int iconSize = 16;
        while (iconSize <= 1024) {
            stage.getIcons().add(new Image(Resources.getResourceAsStream("assets/icon-rounded-" + iconSize + ".png")));
            iconSize *= 2;
        }

        // register event listeners
        DefaultEventBus.getInstance().subscribe(BrowseEvent.class, //
                                                event -> getHostServices().showDocument(event.getUri().toString()));

        // Delays showing the window until the JavaFX thread is ready.
        Platform.runLater(() -> {
            stage.show();
            stage.requestFocus();
        });
    }

    private void loadApplicationProperties() {
        Properties properties = new Properties();
        try (InputStreamReader in = new InputStreamReader(Resources.getResourceAsStream("application.properties"), UTF_8)) {
            properties.load(in);
            properties.forEach((key, value) -> System.setProperty(String.valueOf(key), String.valueOf(value)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
