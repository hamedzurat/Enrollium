package enrollium.client.layout;

import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.NavEvent;
import enrollium.client.page.Page;
import enrollium.client.page.admin.RegistrationStatus;
import enrollium.client.page.admin.SendNotification;
import enrollium.client.page.admin.ServerStats;
import enrollium.client.page.admin.WithdrawRequests;
import enrollium.client.page.database.*;
import enrollium.client.page.home.*;
import enrollium.client.page.students.*;
import enrollium.design.system.i18n.TranslationKey;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.*;


public class MainModel {
    public static final  Class<? extends Page>                        DEFAULT_PAGE = About.class;
    public static final  Set<Class<? extends Page>>                   TAGGED_PAGES = Set.of(LogIn.class);
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

        List<Class<? extends Page>> pages = List.of(
                // Home
                LogIn.class, About.class, UserInfo.class, SignUp.class, ForgotPassword.class, Chat.class,

                // Student
                OfferedCoursePage.class, CourseSchedulePage.class, TradeSection.class, RequestWithdraw.class, History.class, TradeSection.class, Routine.class,

                // Admin
                ServerStats.class, SendNotification.class, RegistrationStatus.class, WithdrawRequests.class,

                // DB
                CoursePage.class, FacultyPage.class, NotificationPage.class, PrerequisitePage.class, SectionPage.class, SpaceTimePage.class, StudentPage.class, SubjectPage.class, TrimesterPage.class //
        );

        // Populate map using reflection
        for (Class<? extends Page> page : pages) {
            try {
                TranslationKey name = (TranslationKey) page.getField("NAME").get(null);
                map.put(page, NavTree.Item.page(name, page));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Error accessing NAME field of " + page.getSimpleName(), e);
            }
        }

        return map;
    }

    // Constructing the Sidebar Tree
    private NavTree.Item createTree() {
        var home = NavTree.Item.group(TranslationKey.Home, new FontIcon(Material2OutlinedAL.HOME));
        home.getChildren().setAll( //
                NAV_TREE.get(About.class), //
                NAV_TREE.get(LogIn.class), //
                NAV_TREE.get(UserInfo.class), //
                NAV_TREE.get(SignUp.class), //
                NAV_TREE.get(ForgotPassword.class), //
                NAV_TREE.get(Chat.class) //
        );
        home.setExpanded(true);

        var student = NavTree.Item.group(TranslationKey.Student, new FontIcon(Material2OutlinedMZ.PERM_IDENTITY));
        student.getChildren().setAll( //
                NAV_TREE.get(Routine.class), //
                NAV_TREE.get(History.class), //
                NAV_TREE.get(OfferedCoursePage.class), //
                NAV_TREE.get(RequestWithdraw.class), //
                NAV_TREE.get(CourseSchedulePage.class), //
                NAV_TREE.get(TradeSection.class) //
        );
        student.setExpanded(true);

        var admin = NavTree.Item.group(TranslationKey.Admin, new FontIcon(Material2OutlinedMZ.SECURITY));
        admin.getChildren().setAll( //
                NAV_TREE.get(ServerStats.class), //
                NAV_TREE.get(SendNotification.class), //
                NAV_TREE.get(WithdrawRequests.class), //
                NAV_TREE.get(RegistrationStatus.class) //
        );
        admin.setExpanded(true);

        var db = NavTree.Item.group(TranslationKey.Database, new FontIcon(Material2OutlinedAL.FOLDER));
        db.getChildren().setAll( //
                NAV_TREE.get(CoursePage.class), //
                NAV_TREE.get(FacultyPage.class), //
                NAV_TREE.get(NotificationPage.class), //
                NAV_TREE.get(PrerequisitePage.class), //
                NAV_TREE.get(SectionPage.class), //
                NAV_TREE.get(SpaceTimePage.class), //
                NAV_TREE.get(StudentPage.class), //
                NAV_TREE.get(SubjectPage.class), //
                NAV_TREE.get(TrimesterPage.class) //
        );
        db.setExpanded(false);

        var root = NavTree.Item.root();
        root.getChildren().setAll(home, student, admin, db);

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
