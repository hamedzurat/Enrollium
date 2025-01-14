package enrollium.client.layout;

import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.NavEvent;
import enrollium.client.page.Page;
import enrollium.client.page.debug.Button;
import enrollium.client.page.general.Advising;
import enrollium.client.page.general.ChangePass;
import enrollium.client.page.general.ThemePage;
import enrollium.client.page.general.UserInfo;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainModel {
    public static final Class<? extends Page> DEFAULT_PAGE = Button.class;
    private static final Map<Class<? extends Page>, NavTree.Item> NAV_TREE = createNavItems();
    private final ReadOnlyObjectWrapper<Class<? extends Page>> selectedPage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<NavTree.Item> navTree = new ReadOnlyObjectWrapper<>(createTree());

    public MainModel() {
        // Load page if NavEvent happens
        DefaultEventBus.getInstance().subscribe(NavEvent.class, e -> navigate(e.getPage()));
    }

    // Creates a map of page classes to navigation items.
    public static Map<Class<? extends Page>, NavTree.Item> createNavItems() {
        var map = new HashMap<Class<? extends Page>, NavTree.Item>();

        // debug
        map.put(Button.class, NavTree.Item.page(Button.NAME, Button.class));

        // general
        map.put(ThemePage.class, NavTree.Item.page(ThemePage.NAME, ThemePage.class));

        // advising
        map.put(Advising.class, NavTree.Item.page("Advising UI", Advising.class));

        // change password
        map.put(ChangePass.class, NavTree.Item.page("Change Password", ChangePass.class));

        // user info
        map.put(UserInfo.class, NavTree.Item.page("User Info", UserInfo.class));

        return map;
    }

    // Constructing the Sidebar Tree
    private NavTree.Item createTree() {
        var general = NavTree.Item.group("General", new FontIcon(Material2OutlinedMZ.SPEED));
        general.getChildren().setAll(NAV_TREE.get(ThemePage.class));
        general.setExpanded(true);

        var advising = NavTree.Item.group("Advising", new FontIcon(Material2OutlinedMZ.POOL));
        advising.getChildren().add(NAV_TREE.get(Advising.class));
        advising.setExpanded(true);

        var changepass = NavTree.Item.group("Change Password", new FontIcon(Material2OutlinedMZ.PUBLIC_OFF));
        changepass.getChildren().add(NAV_TREE.get(ChangePass.class));
        changepass.setExpanded(true);

        var userInfo = NavTree.Item.group("My Info", new FontIcon(Material2OutlinedMZ.PUBLIC));
        userInfo.getChildren().add(NAV_TREE.get(UserInfo.class));
        userInfo.setExpanded(true);

        var debug = NavTree.Item.group("DEBUG", new FontIcon(Material2OutlinedMZ.SETTINGS));
        debug.getChildren().setAll(NAV_TREE.get(Button.class));

        NavTree.Item pageItem = NavTree.Item.page("Theme Settings", new FontIcon(Material2OutlinedMZ.MAIL), ThemePage.class);

        var root = NavTree.Item.root();
        root.getChildren().setAll(debug, general, pageItem, advising, changepass, userInfo);

        return root;
    }

    // Switch page
    public void navigate(Class<? extends Page> page) {
        selectedPage.set(Objects.requireNonNull(page));
    }

    // Sidebar Synchronization
    NavTree.Item getTreeItemForPage(Class<? extends Page> pageClass) {
        return NAV_TREE.getOrDefault(pageClass, NAV_TREE.get(DEFAULT_PAGE));
    }

    // for search dialog
    List<NavTree.Item> findPages(String filter) {
        return NAV_TREE
                .values()
                .stream()
                .filter(item -> item.getValue() != null && item.getValue().matches(filter))
                .toList();
    }

    // exposes observable
    public ReadOnlyObjectProperty<Class<? extends Page>> selectedPageProperty() {
        return selectedPage.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<NavTree.Item> navTreeProperty() {
        return navTree.getReadOnlyProperty();
    }
}
