package enrollium.client;

import atlantafx.base.theme.PrimerDark;
import i18n.I18nManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import settings.SettingsManager;


@Slf4j
public class Main extends Application {
    public static void main(String[] args) {
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/enrollium/client/counter-view.fxml"));
            Parent     root       = fxmlLoader.load();
            Scene      scene      = new Scene(root);

            primaryStage.setTitle("Counter");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            log.error(e.getMessage());
            javafx.application.Platform.exit();
            System.exit(1);
        }
    }
}
