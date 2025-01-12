package enrollium.client.layout;

import atlantafx.base.controls.ModalPane;
import enrollium.client.util.NodeUtils;
import javafx.geometry.Insets;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;


// +--------------------------------------------------+
// |                  ApplicationWindow               |
// |  +----------------------+  +------------------+  |
// |  |      Sidebar         |  |   Page Content   |  |
// |  |   (Navigation)       |  | (Dynamic Pages)  |  |
// |  +----------------------+  +------------------+  |
// |                  ↑ ModalPane (Pop-ups)          |
// +--------------------------------------------------+
public final class ApplicationWindow extends AnchorPane {
    public static final int    MIN_WIDTH     = 1200;
    public static final int    SIDEBAR_WIDTH = 250;
    public static final String MAIN_MODAL_ID = "modal-pane";

    public ApplicationWindow() {
        // Creates a StackPane to layer UI components on top of each other.
        var body = new StackPane();

        // Applies a CSS class "body" for custom styling.
        // // this is the place to apply user custom CSS, one level below the ':root'
        body.getStyleClass().add("body");

        // ModalPane handles pop-up dialogs or modals.
        var modalPane = new ModalPane();
        modalPane.setId(MAIN_MODAL_ID);

        // MainLayer manages main contents.
        body.getChildren().setAll(modalPane, new MainLayer());

        // Ensures the body expands to fill the entire window and set everything to the ApplicationWindow
        NodeUtils.setAnchors(body, Insets.EMPTY);
        getChildren().setAll(body);
    }
}
