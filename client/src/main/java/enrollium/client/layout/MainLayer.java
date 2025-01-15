package enrollium.client.layout;

import enrollium.client.page.Page;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

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
            // Remove the previous page from the view
            final Page prevPage = (Page) subLayerPane
                    .getChildren()
                    .stream()
                    .filter(c -> c instanceof Page)
                    .findFirst()
                    .orElse(null);

            if (prevPage != null) {
                prevPage.reset(); // Clean up the previous page
                subLayerPane.getChildren().remove(prevPage.getView());
            }

            // Load the new page and add it to the StackPane
            final Page        nextPage = pageClass.getDeclaredConstructor().newInstance();
            javafx.scene.Node nextView = nextPage.getView();

            if (nextView == null) {
                throw new RuntimeException("getView() returned null for " + pageClass.getSimpleName());
            }

            // Add the new page view
            subLayerPane.getChildren().clear(); // Clear existing children
            subLayerPane.getChildren().add(nextView);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
