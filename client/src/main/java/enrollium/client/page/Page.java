package enrollium.client.page;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.BBCodeParser;
import enrollium.client.event.BrowseEvent;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.NavEvent;
import enrollium.client.layout.ApplicationWindow;
import enrollium.design.system.i18n.I18nManager;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.datafaker.Faker;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;


// +------------------------------------------------+
// |                  Page Interface                |
// |  +------------------------------------------+  |
// |  |  getName()      →  Page Title           |  |
// |  |  getView()      →  Page Content         |  |
// |  |  reset()        →  Reset Page State     |  |
// |  +------------------------------------------+  |
// |  | createFormattedText() → Clickable Text  |  |
// |  | captionLabel()        → Styled Captions |  |
// |  +------------------------------------------+  |
// |  |   EventBus: BrowseEvent, NavEvent        |  |
// +------------------------------------------------+
public interface Page {
    int    MAX_WIDTH = ApplicationWindow.MIN_WIDTH - ApplicationWindow.SIDEBAR_WIDTH;
    int    HGAP_20   = 20;
    int    HGAP_30   = 30;
    int    VGAP_10   = 10;
    int    VGAP_20   = 20;
    Faker  FAKER     = new Faker();
    Random RANDOM    = new Random();
    TranslationKey getName();
    Parent getView();
    @Nullable Node getSnapshotTarget();
    void reset();
    //
    default <T> List<T> generate(Supplier<T> supplier, int count) {
        return Stream.generate(supplier).limit(count).toList();
    }
    //
    default Feather randomIcon() {
        return Feather.values()[RANDOM.nextInt(Feather.values().length)];
    }
    //
    // Purpose: Creates styled text with clickable links using BBCode formatting.
    // URL Handling:
    //  - Opens external links in a browser.
    //  - Navigates to internal pages using "local://PageName".
    @SuppressWarnings("unchecked")
    default Node createFormattedText(String text, boolean handleUrl) {
        var node = BBCodeParser.createFormattedText(text);

        if (handleUrl) {
            node.addEventFilter(ActionEvent.ACTION, e -> {
                if (e.getTarget() instanceof Hyperlink link && link.getUserData() instanceof String url) {
                    if (url.startsWith("https://") || url.startsWith("http://")) {
                        DefaultEventBus.getInstance().publish(new BrowseEvent(URI.create(url)));
                    }

                    if (url.startsWith("local://")) {
                        try {
                            var rootPackage = "enrollium.client.page.";
                            var c           = Class.forName(rootPackage + url.substring(8));
                            if (Page.class.isAssignableFrom(c)) {
                                DefaultEventBus.getInstance().publish(new NavEvent((Class<? extends Page>) c));
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } catch (Exception ignored) {
                            System.err.println("Invalid local URL: \"" + url + "\"");
                        }
                    }
                }
                e.consume();
            });
        }

        return node;
    }
    //
    // Purpose: Creates a caption with a monospace font for consistency in UI design.
    default Label captionLabel(String text) {
        var label = new Label(text);
        label.setStyle("-fx-font-family:monospace");
        return label;
    }
    //
    class PageHeader extends HBox {
        public PageHeader(Page page) {
            super();

            Objects.requireNonNull(page, "page");

            var titleLbl = new Label();
            titleLbl.getStyleClass().add(Styles.TITLE_1);

            SettingsManager.getInstance()
                           .observe(Setting.LANGUAGE)
                           .distinctUntilChanged()
                           .subscribe(_ -> Platform.runLater(() -> {
                               titleLbl.setText(I18nManager.getInstance().get(page.getName()));
                           }));

            getStyleClass().add("header");
            setSpacing(20);
            getChildren().setAll(titleLbl);
        }
    }
}
