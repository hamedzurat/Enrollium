package enrollium.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    public static void main(String[] args) {
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
        RPCManager.getInstance().shutdown();
    }
}
