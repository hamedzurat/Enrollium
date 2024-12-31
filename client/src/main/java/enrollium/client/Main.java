package enrollium.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    private StudentController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/enrollium/client/student.fxml"));
        Parent     root   = loader.load();
        controller = loader.getController();

        stage.setTitle("Student Manager");
        stage.setScene(new Scene(root, 400, 300));
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }
}
