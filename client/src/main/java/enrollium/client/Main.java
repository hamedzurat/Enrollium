package enrollium.client;

import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            CSSFX.start();

            // Load the FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/enrollium/client/counter-view.fxml"));
            Parent     root       = fxmlLoader.load();
            Scene      scene      = new Scene(root);

            UserAgentBuilder.builder()
                            .themes(JavaFXThemes.MODENA)
                            .themes(MaterialFXStylesheets.forAssemble(true))
                            .setDeploy(true)
                            .setResolveAssets(true)
                            .build()
                            .setGlobal();

            primaryStage.setTitle("Counter");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
