package enrollium.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/enrollium/client/Advising.fxml"));
        VBox root = fxmlLoader.load();  // Assuming root element is VBox
        Scene scene = new Scene(root);

        // Get screen dimensions
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();

        // Set initial window size to 70% of screen size
        primaryStage.setWidth(screenWidth * 0.7);
        primaryStage.setHeight(screenHeight * 0.7);

        // Set minimum and maximum sizes for the window
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setMaxWidth(screenWidth);
        primaryStage.setMaxHeight(screenHeight);

        // Full-screen toggle using F11 key press
        scene.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("F11")) {
                // Toggle full-screen mode
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            }
        });

        // Full-screen listener to manage content resizing
        primaryStage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Full screen mode - resize to full screen size
                primaryStage.setWidth(screenWidth);
                primaryStage.setHeight(screenHeight);
            } else {
                // Exit full-screen mode - restore window size to 70% of screen
                primaryStage.setWidth(screenWidth * 0.7);
                primaryStage.setHeight(screenHeight * 0.7);
            }
        });

        // Set the scene and display the window
        primaryStage.setResizable(true);
        primaryStage.setTitle("Enrollium - Advanced Course Registration System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
