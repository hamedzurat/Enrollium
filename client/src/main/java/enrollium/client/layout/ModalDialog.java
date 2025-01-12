package enrollium.client.layout;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Spacer;
import atlantafx.base.controls.Tile;
import atlantafx.base.layout.ModalBox;
import atlantafx.base.theme.Tweaks;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


// +--------------------------------------------+
// |              ModalDialog                   |
// |  +--------------------------------------+  |
// |  |               Header (Tile)          |  |
// |  +--------------------------------------+  |
// |  |              Content (Card)          |  |
// |  |                                      |  |
// |  +--------------------------------------+  |
// |  |      [Spacer]      [Close Button]    |  |
// |  +--------------------------------------+  |
// +--------------------------------------------+
public abstract class ModalDialog extends ModalBox {
    protected final Card content = new Card();
    protected final Tile header  = new Tile();

    public ModalDialog() {
        // Calls the ModalBox constructor with the ID of the modal pane (#modal-pane), linking this dialog to the main modal container in the UI.
        super("#" + ApplicationWindow.MAIN_MODAL_ID);
        createView();
    }

    // Displays the dialog on the screen.
    public void show(Scene scene) {
        var modalPane = (ModalPane) scene.lookup("#" + ApplicationWindow.MAIN_MODAL_ID);
        modalPane.show(this);
    }

    protected void createView() {
        content.setHeader(header);
        content.getStyleClass().add(Tweaks.EDGE_TO_EDGE);

        // IMPORTANT: this guarantees client will use correct width and height
        setMinWidth(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMaxHeight(USE_PREF_SIZE);

        // Pins the content to all sides of its container for full expansion.
        AnchorPane.setTopAnchor(content, 0d);
        AnchorPane.setRightAnchor(content, 0d);
        AnchorPane.setBottomAnchor(content, 0d);
        AnchorPane.setLeftAnchor(content, 0d);

        addContent(content);
        getStyleClass().add("modal-dialog");
    }

    protected HBox createDefaultFooter() {
        var closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("form-action");
        closeBtn.setCancelButton(true);
        closeBtn.setOnAction(_ -> close());

        var footer = new HBox(10, new Spacer(), closeBtn);
        footer.getStyleClass().add("footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        VBox.setVgrow(footer, Priority.NEVER);

        return footer;
    }
}
