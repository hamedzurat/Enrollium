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
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.*;


public class MainModel {
    public static final  Class<? extends Page>                        DEFAULT_PAGE = LogIn.class;
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

        // Home
        map.put(LogIn.class, NavTree.Item.page(LogIn.NAME, LogIn.class));
        map.put(About.class, NavTree.Item.page(About.NAME, About.class));
        map.put(UserInfo.class, NavTree.Item.page(UserInfo.NAME, UserInfo.class));
        map.put(SignUp.class, NavTree.Item.page(SignUp.NAME, SignUp.class));
        map.put(ForgotPassword.class, NavTree.Item.page(ForgotPassword.NAME, ForgotPassword.class));

        // Student
        map.put(OfferedCoursePage.class, NavTree.Item.page(OfferedCoursePage.NAME, OfferedCoursePage.class));
        map.put(CourseSchedulePage.class, NavTree.Item.page(CourseSchedulePage.NAME, CourseSchedulePage.class));
        map.put(TradeSection.class, NavTree.Item.page(TradeSection.NAME, TradeSection.class));
        map.put(RequestWithdraw.class, NavTree.Item.page(RequestWithdraw.NAME, RequestWithdraw.class));
        map.put(History.class, NavTree.Item.page(History.NAME, History.class));
        map.put(Routine.class, NavTree.Item.page(Routine.NAME, Routine.class));

        // Admin
        map.put(ServerStats.class, NavTree.Item.page(ServerStats.NAME, ServerStats.class));
        map.put(SendNotification.class, NavTree.Item.page(SendNotification.NAME, SendNotification.class));
        map.put(RegistrationStatus.class, NavTree.Item.page(RegistrationStatus.NAME, RegistrationStatus.class));
        map.put(WithdrawRequests.class, NavTree.Item.page(WithdrawRequests.NAME, WithdrawRequests.class));

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
        map.put(TrimesterPage.class, NavTree.Item.page(TrimesterPage.NAME, TrimesterPage.class));

        return map;
    }

    // Constructing the Sidebar Tree
    private NavTree.Item createTree() {
        var home = NavTree.Item.group("Home", new FontIcon(Material2OutlinedAL.HOME));
        home.getChildren().setAll( //
                NAV_TREE.get(About.class), //
                NAV_TREE.get(LogIn.class), //
                NAV_TREE.get(UserInfo.class), //
                NAV_TREE.get(SignUp.class), //
                NAV_TREE.get(ForgotPassword.class) //
        );
        home.setExpanded(true);

        var student = NavTree.Item.group("Student", new FontIcon(Material2OutlinedMZ.PERM_IDENTITY));
        student.getChildren().setAll( //
                NAV_TREE.get(Routine.class), //
                NAV_TREE.get(History.class), //
                NAV_TREE.get(OfferedCoursePage.class), //
                NAV_TREE.get(RequestWithdraw.class), //
                NAV_TREE.get(CourseSchedulePage.class), //
                NAV_TREE.get(TradeSection.class) //
        );
        student.setExpanded(true);

        var admin = NavTree.Item.group("Admin", new FontIcon(Material2OutlinedMZ.SECURITY));
        admin.getChildren().setAll( //
                NAV_TREE.get(ServerStats.class), //
                NAV_TREE.get(SendNotification.class), //
                NAV_TREE.get(WithdrawRequests.class), //
                NAV_TREE.get(RegistrationStatus.class) //
        );
        admin.setExpanded(true);

        var db = NavTree.Item.group("Database", new FontIcon(Material2OutlinedAL.FOLDER));
        db.getChildren().setAll( //
                NAV_TREE.get(CoursePage.class), //
                NAV_TREE.get(FacultyPage.class), //
                NAV_TREE.get(NotificationPage.class), //
                NAV_TREE.get(PrerequisitePage.class), //
                NAV_TREE.get(SectionPage.class), //
                NAV_TREE.get(SpaceTimePage.class), //
                NAV_TREE.get(StudentPage.class), //
                NAV_TREE.get(SubjectPage.class), //
                NAV_TREE.get(UserPage.class), //
                NAV_TREE.get(TrimesterPage.class) //
        );

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
