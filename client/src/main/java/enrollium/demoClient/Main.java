package enrollium.demoClient;

import atlantafx.base.theme.PrimerDark;
import banner.Issue;
import i18n.I18nManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import settings.SettingsManager;
import version.Version;


@Slf4j
public class Main extends Application {
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
    public void start(Stage primaryStage) {
        try {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

            // Load the FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/enrollium/demoClient/counter-view.fxml"));
            Parent     root       = fxmlLoader.load();
            Scene      scene      = new Scene(root);

            primaryStage.setTitle("Counter");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            log.error(e.getMessage());
            Platform.exit();
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
