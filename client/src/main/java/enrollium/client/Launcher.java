package enrollium.client;

import enrollium.client.event.BrowseEvent;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.HotkeyEvent;
import enrollium.client.layout.ApplicationWindow;
import enrollium.client.theme.ThemeManager;
import enrollium.design.system.i18n.I18nManager;
import enrollium.design.system.settings.SettingsManager;
import enrollium.lib.banner.Issue;
import enrollium.lib.version.Version;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;


@Slf4j
public class Launcher extends Application {
    public static final List<KeyCodeCombination> SUPPORTED_HOTKEYS = List.of(new KeyCodeCombination(KeyCode.SLASH), new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));

    public static void main(String[] args) {
        Issue.print(log);
        log.info("[VERSION]: {}", Version.getVersion());

        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();

        SettingsManager.BlockingInit();
        I18nManager.BlockingInit();
    }

    @Override
    public void start(Stage stage) {
        // Sets a global exception handler (DefaultExceptionHandler).
        Thread.currentThread().setUncaughtExceptionHandler(new DefaultExceptionHandler(stage));
        // Loads application configuration from application.properties.
        loadApplicationProperties();

        // Creates the main window with size
        var window = new ApplicationWindow();
        var scene  = new Scene(window, ApplicationWindow.MIN_WIDTH + 80, 768);

        // Initialize ThemeManager first
        var tm = ThemeManager.getInstance();
        tm.initialize(scene);

        // Binds key press events
        scene.setOnKeyPressed(event -> {
            for (KeyCodeCombination k : SUPPORTED_HOTKEYS) {
                if (k.match(event)) {
                    DefaultEventBus.getInstance().publish(new HotkeyEvent(k));
                    return;
                }
            }
        });

        // Stage setup
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

        // Register event listeners
        DefaultEventBus.getInstance().subscribe(BrowseEvent.class,
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

    @Override
    public void stop() {
        try {
            log.info("Application shutting down...");
            SettingsManager.getInstance().shutdown();
            log.info("Application shutdown complete");
        } catch (Exception e) {
            log.error("Error during shutdown", e);
        } finally {
            Platform.exit();
        }
    }
}
