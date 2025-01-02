package enrollium.client;

import client.ClientRPC;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import version.Version;


@Slf4j
public class Main extends Application {
    public static void main(String[] args) {
        log.info("[VERSION]: {}", Version.getVersion());
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/enrollium/client/login.fxml"));
        Parent     root   = loader.load();

        stage.setTitle("Enrollium Login");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public void stop() {
        if (ClientRPC.getInstance() != null) ClientRPC.getInstance().close();
    }
}
