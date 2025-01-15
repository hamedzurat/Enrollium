package enrollium.client.page.general;

import enrollium.client.page.OutlinePage;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Nullable;

public class SectionFormView extends OutlinePage {

    public static final String NAME = "Section Form View";

    public SectionFormView() {
        super();

        // Add page header
        addPageHeader();

        // Add section for form
        addSection("Section Form", createFormView());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public @Nullable Node getSnapshotTarget() {
        return null;
    }

    @Override
    public void reset() {
        // Reset logic if needed
    }

    private VBox createFormView() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));

        // Create form fields
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label sectionNameLabel = new Label("Section Name:");
        TextField sectionNameField = new TextField();

        Label sectionIdLabel = new Label("Section ID:");
        TextField sectionIdField = new TextField();

        Label subjectLabel = new Label("Subject:");
        TextField subjectField = new TextField();

        Label trimesterLabel = new Label("Trimester:");
        TextField trimesterField = new TextField();

        Label maxCapacityLabel = new Label("Max Capacity:");
        TextField maxCapacityField = new TextField();

        Label currentCapacityLabel = new Label("Current Capacity:");
        TextField currentCapacityField = new TextField();

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(event -> {
            // Logic to handle form submission
            System.out.println("Section Name: " + sectionNameField.getText());
            System.out.println("Section ID: " + sectionIdField.getText());
            System.out.println("Subject: " + subjectField.getText());
            System.out.println("Trimester: " + trimesterField.getText());
            System.out.println("Max Capacity: " + maxCapacityField.getText());
            System.out.println("Current Capacity: " + currentCapacityField.getText());
        });

        // Add form fields to the grid
        form.add(sectionNameLabel, 0, 0);
        form.add(sectionNameField, 1, 0);
        form.add(sectionIdLabel, 0, 1);
        form.add(sectionIdField, 1, 1);
        form.add(subjectLabel, 0, 2);
        form.add(subjectField, 1, 2);
        form.add(trimesterLabel, 0, 3);
        form.add(trimesterField, 1, 3);
        form.add(maxCapacityLabel, 0, 4);
        form.add(maxCapacityField, 1, 4);
        form.add(currentCapacityLabel, 0, 5);
        form.add(currentCapacityField, 1, 5);

        // Add form and submit button to the VBox
        formBox.getChildren().addAll(form, submitButton);

        return formBox;
    }
}
