package enrollium.client.page.general;

import enrollium.client.page.OutlinePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


public class SpaceTimeFormView extends OutlinePage {
    public static final TranslationKey NAME = TranslationKey.SPACETIME;

    public SpaceTimeFormView() {
        super();

        // Add page header
        addPageHeader();

        // Add a section for the form
        addSection("SpaceTime Form", createFormView());
    }

    @Override
    protected void updateTexts() {

    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createFormView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        // Create form fields
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label     roomNameLabel = new Label("Room Name:");
        TextField roomNameField = new TextField();

        Label     roomNumberLabel = new Label("Room Number:");
        TextField roomNumberField = new TextField();

        Label     roomTypeLabel = new Label("Room Type:");
        TextField roomTypeField = new TextField();

        Label     dayLabel = new Label("Day:");
        TextField dayField = new TextField();

        Label     timeSlotLabel = new Label("Time Slot:");
        TextField timeSlotField = new TextField();

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(event -> {
            // Logic to handle form submission
            System.out.println("Room Name: " + roomNameField.getText());
            System.out.println("Room Number: " + roomNumberField.getText());
            System.out.println("Room Type: " + roomTypeField.getText());
            System.out.println("Day: " + dayField.getText());
            System.out.println("Time Slot: " + timeSlotField.getText());
        });

        // Add form fields to grid
        form.add(roomNameLabel, 0, 0);
        form.add(roomNameField, 1, 0);
        form.add(roomNumberLabel, 0, 1);
        form.add(roomNumberField, 1, 1);
        form.add(roomTypeLabel, 0, 2);
        form.add(roomTypeField, 1, 2);
        form.add(dayLabel, 0, 3);
        form.add(dayField, 1, 3);
        form.add(timeSlotLabel, 0, 4);
        form.add(timeSlotField, 1, 4);

        root.getChildren().addAll(form, submitButton);

        return root;
    }
}
