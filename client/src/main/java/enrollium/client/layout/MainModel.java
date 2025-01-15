package enrollium.client.layout;

import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.NavEvent;
import enrollium.client.page.Page;
import enrollium.client.page.database.*;
import enrollium.client.page.debug.Button;
import enrollium.client.page.general.*;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainModel {
    public static final  Class<? extends Page>                        DEFAULT_PAGE = Button.class;
    private static final Map<Class<? extends Page>, NavTree.Item>     NAV_TREE     = createNavItems();
    private final        ReadOnlyObjectWrapper<Class<? extends Page>> selectedPage = new ReadOnlyObjectWrapper<>();
    private final        ReadOnlyObjectWrapper<NavTree.Item>          navTree      = new ReadOnlyObjectWrapper<>(createTree());

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
        map.put(Login.class, NavTree.Item.page(Login.NAME, Login.class));

        // new pages
        map.put(SpaceTimeFormView.class, NavTree.Item.page(SpaceTimeFormView.NAME, SpaceTimeFormView.class));
        map.put(SpaceTimeTableView.class, NavTree.Item.page(SpaceTimeTableView.NAME, SpaceTimeTableView.class));
        map.put(SectionFormView.class, NavTree.Item.page(SectionFormView.NAME, SectionFormView.class));
        map.put(SectionTableView.class, NavTree.Item.page(SectionTableView.NAME, SectionTableView.class));

        // DB
        map.put(CoursePage.class, NavTree.Item.page(CoursePage.NAME, CoursePage.class));
        map.put(FacultyPage.class, NavTree.Item.page(FacultyPage.NAME, FacultyPage.class));
        map.put(NotificationPage.class, NavTree.Item.page(NotificationPage.NAME, NotificationPage.class));
        map.put(PrerequisitePage.class, NavTree.Item.page(PrerequisitePage.NAME, PrerequisitePage.class));
        map.put(SectionPage.class, NavTree.Item.page(SectionPage.NAME, SectionPage.class));
        map.put(SpaceTimePage.class, NavTree.Item.page(SpaceTimePage.NAME, SpaceTimePage.class));
        map.put(StudentPage.class, NavTree.Item.page(StudentPage.NAME, StudentPage.class));
        map.put(SubjectPage.class, NavTree.Item.page(SubjectPage.NAME, SubjectPage.class));
        map.put(UserPage.class, NavTree.Item.page(UserPage.NAME, UserPage.class));

        return map;
    }

    // Constructing the Sidebar Tree
    private NavTree.Item createTree() {
        var general = NavTree.Item.group("General", new FontIcon(Material2OutlinedMZ.SPEED));
        general.getChildren().setAll(NAV_TREE.get(Login.class));
        general.setExpanded(true);

        var spaceTime = NavTree.Item.group("SpaceTime", new FontIcon(Material2OutlinedMZ.PUBLIC));
        spaceTime.getChildren().setAll(NAV_TREE.get(SpaceTimeFormView.class), NAV_TREE.get(SpaceTimeTableView.class));
        spaceTime.setExpanded(true);

        var sections = NavTree.Item.group("Sections", new FontIcon(Material2OutlinedMZ.UPGRADE));
        sections.getChildren().setAll(NAV_TREE.get(SectionFormView.class), NAV_TREE.get(SectionTableView.class));
        sections.setExpanded(true);

        var debug = NavTree.Item.group("DEBUG", new FontIcon(Material2OutlinedMZ.SETTINGS));
        debug.getChildren().setAll(NAV_TREE.get(Button.class));

        var db = NavTree.Item.group("Database", new FontIcon(Material2OutlinedMZ.SETTINGS));
        db.getChildren()
          .setAll(NAV_TREE.get(CoursePage.class), NAV_TREE.get(FacultyPage.class), NAV_TREE.get(NotificationPage.class), NAV_TREE.get(PrerequisitePage.class), NAV_TREE.get(SectionPage.class), NAV_TREE.get(SpaceTimePage.class), NAV_TREE.get(StudentPage.class), NAV_TREE.get(SubjectPage.class), NAV_TREE.get(UserPage.class));

        var root = NavTree.Item.root();
        root.getChildren().setAll(debug, general, spaceTime, sections, db);

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
        return NAV_TREE.values()
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
