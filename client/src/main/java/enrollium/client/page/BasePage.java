package enrollium.client.page;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import enrollium.client.page.general.NotificationType;
import enrollium.design.system.i18n.I18nManager;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;


public abstract class BasePage extends StackPane implements Page {
    private static final double             NOTIFICATION_SPACING = 10.0;
    private static final double             NOTIFICATION_HEIGHT  = 60.0;  // Estimated notification height
    protected final      ScrollPane         scrollPane           = new ScrollPane();
    protected final      VBox               userContent          = new VBox();
    protected final      StackPane          userContentArea      = new StackPane(userContent);
    private final        SettingsManager    settings             = SettingsManager.getInstance();
    private final        I18nManager        i18nManager          = I18nManager.getInstance();
    private final        Label              titleLbl             = new Label();
    private final        List<Notification> activeNotifications  = new ArrayList<>();
    protected            boolean            isRendered           = false;

    protected BasePage() {
        super();
        userContent.getStyleClass().add("user-content");
        getStyleClass().add("base-page");
        createPageLayout();

        settings.observe(Setting.LANGUAGE).distinctUntilChanged().subscribe(_ -> Platform.runLater(this::updateTexts));
        updateTexts();
    }

    protected void updateTexts() {
        titleLbl.setText(i18nManager.get(this.getName()));
    }

    protected void createPageLayout() {
        scrollPane.setContent(userContentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPadding(new Insets(20));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        StackPane contentWrapper = new StackPane(scrollPane);
        contentWrapper.setAlignment(Pos.CENTER);
        contentWrapper.setPadding(new Insets(0));
        VBox.setVgrow(contentWrapper, Priority.ALWAYS);

        VBox mainContainer = new VBox(contentWrapper);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        setMinWidth(Page.MAX_WIDTH);
        getChildren().add(mainContainer);
    }

    protected void addPageHeader() {
        titleLbl.getStyleClass().addAll(Styles.TITLE_1);

        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setSpacing(20);
        header.getChildren().setAll(titleLbl);
        header.setPadding(new Insets(0, 0, 30, 0));

        userContent.getChildren().add(header);
        updateTexts();
    }

    protected void addNode(Node node) {
        userContent.getChildren().add(node);
    }

    protected void addFormattedText(String text) {
        TextFlow description = createFormattedText(text, true);
        description.getStyleClass().add(Styles.TITLE_4);
        description.setPadding(new Insets(0, 0, 40, 0));
        userContent.getChildren().add(description);
    }

    protected void addSection(String title, Node content) {
        var titleIcon = new FontIcon(Feather.HASH);
        titleIcon.getStyleClass().add("icon-subtle");

        var titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_2);
        titleLabel.setGraphic(titleIcon);
        titleLabel.setGraphicTextGap(10);
        titleLabel.setPadding(new Insets(0, 0, 0, 0));

        VBox section = new VBox(10, titleLabel, content);
        section.setPadding(new Insets(0, 0, 30, 0));
        VBox.setVgrow(content, Priority.ALWAYS);

        userContent.getChildren().add(section);
    }

    @Override
    public Pane getView() {
        return this;
    }

    @Override
    public Node getSnapshotTarget() {
        return userContentArea;
    }

    @Override
    public void reset() {}

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (isRendered) return;
        isRendered = true;
        onRendered();
    }

    protected void onRendered() {}

    protected void showNotification(String message, NotificationType type) {
        showNotification(message, type, Duration.seconds(3));
    }

    protected void showNotification(String message, NotificationType type, Duration duration) {
        Notification notification = new Notification(message, new FontIcon(type.getIcon()));
        notification.getStyleClass().addAll(type.getStyleClass(), Styles.ELEVATED_1);

        notification.setPrefHeight(Region.USE_PREF_SIZE);
        notification.setMaxHeight(Region.USE_PREF_SIZE);

        // Manage notification stacking (max 3)
        if (activeNotifications.size() >= 3) {
            Notification oldest = activeNotifications.remove(0);
            getChildren().remove(oldest);
        }

        activeNotifications.add(notification);
        getChildren().add(notification);

        // Stack vertically by adjusting the top margin
        updateNotificationPositions();

        // Slide-in animation
        Animations.slideInDown(notification, Duration.millis(250)).playFromStart();

        // Auto-dismiss after duration
        PauseTransition delay = new PauseTransition(duration != null ? duration : Duration.seconds(3));
        delay.setOnFinished(e -> {
            var out = Animations.slideOutUp(notification, Duration.millis(250));
            out.setOnFinished(ev -> {
                getChildren().remove(notification);
                activeNotifications.remove(notification);
                updateNotificationPositions();
            });
            out.playFromStart();
        });
        delay.play();
    }

    // Adjusts the vertical position of stacked notifications
    private void updateNotificationPositions() {
        for (int i = 0; i < activeNotifications.size(); i++) {
            Notification ntf       = activeNotifications.get(i);
            double       topMargin = 10 + i * (NOTIFICATION_HEIGHT + NOTIFICATION_SPACING);
            StackPane.setAlignment(ntf, Pos.TOP_RIGHT);
            StackPane.setMargin(ntf, new Insets(topMargin, 10, 0, 0));
        }
    }
}
