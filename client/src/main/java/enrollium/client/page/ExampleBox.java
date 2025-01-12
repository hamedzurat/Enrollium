package enrollium.client.page;

import atlantafx.base.controls.Spacer;
import atlantafx.base.controls.ToggleSwitch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Nullable;


// This widget emulates TabPane behavior, because TabPane itself doesn't work as it should:
// https://bugs.openjdk.org/browse/JDK-8145490
public final class ExampleBox extends VBox {
    private final ToggleSwitch stateToggle;

    public ExampleBox(Node preview) {
        this(preview, null);
    }

    public ExampleBox(@Nullable Node preview, @Nullable Node description) {
        super();

        stateToggle = new ToggleSwitch();
        HBox.setMargin(stateToggle, new Insets(0, 0, 0, 10));

        var tabs = new HBox(new Label("Disable/or not"), new Spacer(), stateToggle);
        tabs.getStyleClass().add("tabs");
        tabs.setAlignment(Pos.CENTER_LEFT);

        var content = new VBox();

        getStyleClass().add("example-box");
        if (description != null) {
            getChildren().add(description);
        }

        if (preview != null) {
            getChildren().addAll(tabs, content);
            content.getChildren().setAll(preview);
        } else {
            getChildren().addAll(content);
        }

        stateToggle.setDisable(false);
        stateToggle.selectedProperty().addListener((_, _, val) -> content
                .getChildren()
                .forEach(c -> c.setDisable(val)));
    }

    public void setAllowDisable(boolean allow) {
        stateToggle.setDisable(!allow);
        stateToggle.setVisible(allow);
        stateToggle.setManaged(allow);
    }
}
