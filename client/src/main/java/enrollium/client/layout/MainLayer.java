package enrollium.client.layout;

import enrollium.client.page.Page;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

import static javafx.scene.layout.Priority.ALWAYS;


// +--------------------------------------------------+
// |                  MainLayer                       |
// |  +----------------------+  +------------------+  |
// |  |      Sidebar         |  |   Content Area   |  |
// |  |   (Navigation)       |  |  (Dynamic Pages) |  |
// |  +----------------------+  +------------------+  |
// |    (Fixed Width: 250px)        (Expandable)      |
// +--------------------------------------------------+
// BorderPane: A JavaFX layout that arranges components in five regions: top, bottom, left, right, and center.
class MainLayer extends BorderPane {
    static final  int       PAGE_TRANSITION_DURATION_MS = 500;
    private final MainModel model                       = new MainModel();
    private final Sidebar   sidebar                     = new Sidebar(model);
    private final StackPane subLayerPane                = new StackPane();

    public MainLayer() {
        super();

        createView();     // Sets up the sidebar and content pane
        initListeners();  // Binds navigation events to UI updates

        model.navigate(MainModel.DEFAULT_PAGE);  // Loads the default page

        Platform.runLater(sidebar::begForFocus); // Ensures the sidebar can capture keyboard input
    }

    private void createView() {
        sidebar.setMinWidth(ApplicationWindow.SIDEBAR_WIDTH);
        sidebar.setMaxWidth(ApplicationWindow.SIDEBAR_WIDTH);

        HBox.setHgrow(subLayerPane, ALWAYS);

        setId("main");
        setLeft(sidebar);
        setCenter(subLayerPane);
    }

    private void initListeners() {
        model.selectedPageProperty().addListener((obs, old, val) -> {
            if (val != null) loadPage(val);
        });
    }

    private void loadPage(Class<? extends Page> pageClass) {
        try {
            final Page prevPage = (Page) subLayerPane
                    .getChildren()
                    .stream()
                    .filter(c -> c instanceof Page)
                    .findFirst()
                    .orElse(null);
            final Page nextPage = pageClass.getDeclaredConstructor().newInstance();

            // For first time, no animation
            if (getScene() == null) subLayerPane.getChildren().add(nextPage.getView());
            else {
                Objects.requireNonNull(prevPage);

                prevPage.reset();  // Clean up the previous page

                // animate switching between pages
                subLayerPane.getChildren().add(nextPage.getView());
                subLayerPane.getChildren().remove(prevPage.getView());
                var transition = new FadeTransition(Duration.millis(PAGE_TRANSITION_DURATION_MS), nextPage.getView());
                transition.setFromValue(0.0);
                transition.setToValue(1.0);
                transition.setOnFinished(_ -> {
                    if (nextPage instanceof Pane nextPane) nextPane.toFront();
                });
                transition.play();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
