package enrollium.client.page;

import atlantafx.base.theme.Styles;
import enrollium.design.system.i18n.I18nManager;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;


public abstract class BasePage extends StackPane implements Page {
    protected final ScrollPane      scrollPane      = new ScrollPane();
    protected final VBox            userContent     = new VBox();
    protected final StackPane       userContentArea = new StackPane(userContent);
    private final   SettingsManager settings        = SettingsManager.getInstance();
    private final   I18nManager     i18nManager     = I18nManager.getInstance();
    private final   Label           titleLbl        = new Label();
    protected       boolean         isRendered      = false;

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
}
