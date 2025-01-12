package enrollium.client.layout;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Tweaks;
import enrollium.client.page.Page;
import enrollium.client.util.NodeUtils;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


// Customizes the standard JavaFX TreeView to display navigation items (Nav).
public final class NavTree extends TreeView<Nav> {
    public NavTree(MainModel model) {
        super();

        // Listens for item selection in the sidebar.
        // If the selected item is a page (not a group), it triggers navigation via model.navigate().
        getSelectionModel().selectedItemProperty().addListener((_, _, val) -> {
            if (!(val instanceof Item item)) return;
            if (!item.isGroup()) model.navigate(item.pageClass());
        });

        getStyleClass().addAll(Tweaks.EDGE_TO_EDGE); // Applies the EDGE_TO_EDGE style to the tree for a modern look.
        setShowRoot(false); // Hides the root item with setShowRoot(false) for a cleaner UI.
        rootProperty().bind(model.navTreeProperty()); // Binds the tree's root to model.navTreeProperty(), so it dynamically updates when the navigation tree changes.
        setCellFactory(_ -> new NavTreeCell()); // Uses a custom cell factory (NavTreeCell) to control how each navigation item is displayed.
    }

    public static final class NavTreeCell extends TreeCell<Nav> {
        private static final PseudoClass GROUP = PseudoClass.getPseudoClass("group");
        private final        HBox        root;
        private final        Label       titleLabel;
        private final        Node        arrowIcon;
        private final        Label       tagLabel;

        public NavTreeCell() {
            super();

            titleLabel = new Label();
            titleLabel.setGraphicTextGap(10);
            titleLabel.getStyleClass().add("title");

            arrowIcon = new FontIcon();
            arrowIcon.getStyleClass().add("arrow");

            tagLabel = new Label("new");
            tagLabel.getStyleClass().add("tag");

            root = new HBox();
            root.setAlignment(Pos.CENTER_LEFT);
            root.getChildren().setAll(titleLabel, new Spacer(), arrowIcon, tagLabel);
            root.setCursor(Cursor.HAND);
            root.getStyleClass().add("container");
            root.setMaxWidth(ApplicationWindow.SIDEBAR_WIDTH - 10);

            getStyleClass().add("nav-tree-cell");

            // Clicking a group toggles its expansion
            root.setOnMouseClicked(e -> {
                if (!(getTreeItem() instanceof Item item)) return;

                if (item.isGroup() && e.getButton() == MouseButton.PRIMARY) {
                    item.setExpanded(!item.isExpanded());
                    // scroll slightly above the target
                    getTreeView().scrollTo(getTreeView().getRow(item) - 10);
                }
            });
        }

        @Override
        protected void updateItem(Nav nav, boolean empty) {
            super.updateItem(nav, empty);

            // Checks if the nav item is null or if the cell is marked as empty.
            // Prevents leftover data from being displayed in recycled cells (JavaFX reuses cells for performance).
            if (nav == null || empty) {
                setGraphic(null);
                titleLabel.setText(null);
                titleLabel.setGraphic(null);
            } else {
                // If the item is valid, this sets the entire cell's graphical content (root).
                setGraphic(root);

                // Title and Icon are set from the Nav object.
                titleLabel.setText(nav.title());
                titleLabel.setGraphic(nav.graphic());

                pseudoClassStateChanged(GROUP, nav.isGroup()); // Dynamically applies or removes the CSS pseudo-class GROUP.
                NodeUtils.toggleVisibility(arrowIcon, nav.isGroup()); // Shows the arrow icon for groups and hides it for pages.
                NodeUtils.toggleVisibility(tagLabel, nav.isTagged()); // Displays a tag for pages that are marked.
            }
        }
    }


    public static final class Item extends TreeItem<Nav> {
        private final Nav nav;

        private Item(Nav nav) {
            this.nav = Objects.requireNonNull(nav, "nav");
            setValue(nav);
        }

        // Root Item: Starts the navigation tree.
        public static Item root() {
            return new Item(Nav.ROOT);
        }

        // Group Item: Creates a non-clickable category.
        public static Item group(String title, Node graphic) {
            return new Item(new Nav(title, graphic, null, null));
        }

        // Page Item: Creates a clickable navigation link.
        public static Item page(String title, @Nullable Class<? extends Page> pageClass) {
            Objects.requireNonNull(pageClass, "pageClass");
            return new Item(new Nav(title, null, pageClass, Collections.emptyList()));
        }

        public static Item page(String title, Node graphic, @Nullable Class<? extends Page> pageClass) {
            Objects.requireNonNull(pageClass, "pageClass");
            return new Item(new Nav(title, graphic, pageClass, Collections.emptyList()));
        }

        public static Item page(String title, @Nullable Class<? extends Page> pageClass, String... searchKeywords) {
            Objects.requireNonNull(pageClass, "pageClass");
            return new Item(new Nav(title, null, pageClass, List.of(searchKeywords)));
        }

        public boolean isGroup() {
            return nav.isGroup();
        }

        public @Nullable Class<? extends Page> pageClass() {
            return nav.pageClass();
        }
    }
}
