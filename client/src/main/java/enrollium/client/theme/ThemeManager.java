package enrollium.client.theme;

import atlantafx.base.theme.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import enrollium.client.Resources;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.EventBus;
import enrollium.client.event.ThemeEvent;
import enrollium.client.event.ThemeEvent.EventType;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;


// +-----------------------------------------------+
// |             ThemeManager (Singleton)          |
// |  +-----------------------------------------+  |
// |  |  ThemeRepository  →  Built-in & Custom |  |
// |  +-----------------------------------------+  |
// |  |   Font Size, Font Family, Zoom Levels   |  |
// |  +-----------------------------------------+  |
// |  |   Accent Colors & Smooth Theme Changes  |  |
// |  +-----------------------------------------+  |
// |  |  Publishes Events for UI to Update      |  |
// +-----------------------------------------------+
@Getter
@Setter
@Slf4j
public final class ThemeManager {
    public static final  List<Integer>               SUPPORTED_FONT_SIZE   = IntStream.range(8, 29).boxed().toList();
    public static final  List<Integer>               SUPPORTED_ZOOM        = List.of(50, 75, 80, 90, 100, 110, 125, 150, 175, 200);
    static final         String[]                    APP_STYLESHEETS       = new String[]{Resources.resolve("assets/styles/index.css")};
    static final         Set<Class<? extends Theme>> PROJECT_THEMES        = Set.of(PrimerLight.class, PrimerDark.class, NordLight.class, NordDark.class, CupertinoLight.class, CupertinoDark.class, Dracula.class);
    private static final PseudoClass                 DARK                  = PseudoClass.getPseudoClass("dark");
    private static final PseudoClass                 USER_CUSTOM           = PseudoClass.getPseudoClass("user-custom");
    private static final EventBus                    EVENT_BUS             = DefaultEventBus.getInstance();
    private static final ObjectMapper                MAPPER                = new ObjectMapper();
    private final        Map<String, String>         customCSSDeclarations = new LinkedHashMap<>();
    private final        Map<String, String>         customCSSRules        = new LinkedHashMap<>();
    @Getter
    private final        ThemeRepository             repository            = new ThemeRepository();
    private final        SettingsManager             settingsManager;
    @Getter
    private              String                      fontFamily;
    @Getter
    private              int                         fontSize;
    private              boolean                     initialized           = false;
    @Getter
    private              int                         zoom;
    @Getter
    private              Scene                       scene;
    private              SamplerTheme                currentTheme          = null;
    private              AccentColor                 accentColor           = null;

    private ThemeManager() {
        this.settingsManager = SettingsManager.getInstance();
        log.info("ThemeManager initialized.");
    }

    public static ThemeManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void initialize(Scene scene) {
        this.scene = Objects.requireNonNull(scene);

        initializeFromSettings();
        setupSettingsObservers();
    }

    private void initializeFromSettings() {
        fontFamily = settingsManager.get(Setting.FONT_FAMILY);
        fontSize   = settingsManager.get(Setting.FONT_SIZE);
        zoom       = settingsManager.get(Setting.ZOOM);
        setFontFamily(fontFamily);
        setFontSize(fontSize);
//        setZoom(zoom);

        String themeName = settingsManager.get(Setting.THEME);
        repository.getAll()
                  .stream()
                  .filter(theme -> theme.getName().equals(themeName))
                  .findFirst()
                  .ifPresent(this::setTheme);

        String accentColorHex = settingsManager.get(Setting.ACCENT_COLOR);
        if (!accentColorHex.equals("#FFFFFF")) {
            setAccentColor(new AccentColor(Color.web(accentColorHex), PseudoClass.getPseudoClass("accent-primer-whatever")));
        }

        try {
            String       externalThemesJson = settingsManager.get(Setting.EXTERNAL_THEMES);
            List<String> externalThemePaths = MAPPER.readValue(externalThemesJson, new TypeReference<>() {});
            for (String path : externalThemePaths) {
                try {
                    repository.addFromFile(new java.io.File(path));
                } catch (Exception e) {
                    // Log error but continue
                }
            }
        } catch (JsonProcessingException e) {
            // Handle error
        }
    }

    private void setupSettingsObservers() {
        settingsManager.observe(Setting.FONT_FAMILY).distinctUntilChanged().subscribe(family -> {
            if (!Objects.equals(family, getFontFamily())) setFontFamily((String) family);
        }, error -> log.error("Error in font family observer", error));

        settingsManager.observe(Setting.FONT_SIZE).distinctUntilChanged().subscribe(size -> {
            if (!Objects.equals(size, getFontSize())) setFontSize((Integer) size);
        }, error -> log.error("Error in font size observer", error));

        settingsManager.observe(Setting.THEME).distinctUntilChanged().subscribe(themeName -> {
            if (!Objects.equals(themeName, currentTheme != null ? currentTheme.getName() : null)) {
                repository.getAll()
                          .stream()
                          .filter(theme -> theme.getName().equals(themeName))
                          .findFirst()
                          .ifPresent(this::setTheme);
            }
        }, error -> log.error("Error in theme observer", error));

        settingsManager.observe(Setting.ACCENT_COLOR).distinctUntilChanged().subscribe(colorHex -> {
            String currentHex = accentColor != null ? accentColor.primaryColor().toString() : "#FFFFFF";
            if (!Objects.equals(colorHex, currentHex)) {
                setAccentColor(new AccentColor(Color.web((String) colorHex), PseudoClass.getPseudoClass("accent-primer-whatever")));
            }
        }, error -> log.error("Error in accent color observer", error));

        settingsManager.observe(Setting.ZOOM).distinctUntilChanged().subscribe(zoom -> {
//                if (!Objects.equals(zoom, getZoom())) {
//                    setZoom((Integer) zoom);
//                }
        }, error -> log.error("Error in zoom observer", error));
    }

    public void setScene(Scene scene) {
        this.scene = Objects.requireNonNull(scene);
        if (!initialized) {
            initializeFromSettings();
            setupSettingsObservers();
            initialized = true;
        }
    }

    public SamplerTheme getTheme() {
        return currentTheme;
    }

    public void setTheme(SamplerTheme theme) {
        Objects.requireNonNull(theme);

        if (currentTheme != null) {
            animateThemeChange(Duration.millis(750));
        }

        Application.setUserAgentStylesheet(Objects.requireNonNull(theme.getUserAgentStylesheet()));
        getScene().getStylesheets().setAll(theme.getAllStylesheets());
        getScene().getRoot().pseudoClassStateChanged(DARK, theme.isDarkMode());

        resetAccentColor();
        resetCustomCSS();

        currentTheme = theme;
        EVENT_BUS.publish(new ThemeEvent(EventType.THEME_CHANGE));
    }

    public void setFontFamily(String fontFamily) {
        Objects.requireNonNull(fontFamily);
        setCustomDeclaration("-fx-font-family", "\"" + fontFamily + "\"");
        reloadCustomCSS();
        EVENT_BUS.publish(new ThemeEvent(EventType.FONT_CHANGE));
    }

    public void setFontSize(int size) {
        if (!SUPPORTED_FONT_SIZE.contains(size)) {
            throw new IllegalArgumentException("Invalid font size: " + size);
        }

        setCustomDeclaration("-fx-font-size", size + "px");
        setCustomRule(".ikonli-font-icon", String.format("-fx-icon-size: %dpx;", size + 2));

        int rawZoom = (int) Math.ceil((size * 1.0 / getFontSize()) * 100);
        int zoom = SUPPORTED_ZOOM.stream()
                                 .min(Comparator.comparingInt(i -> Math.abs(i - rawZoom)))
                                 .orElseThrow(NoSuchElementException::new);

        reloadCustomCSS();
        EVENT_BUS.publish(new ThemeEvent(EventType.FONT_CHANGE));
    }

    public void setZoom(int zoom) {
        if (!SUPPORTED_ZOOM.contains(zoom)) {
            throw new IllegalArgumentException("Invalid zoom: " + zoom);
        }

        setFontSize((int) Math.ceil(zoom != 100 ? (getFontSize() * zoom) / 100.0f : getFontSize()));
    }

    public void setAccentColor(AccentColor color) {
        Objects.requireNonNull(color);
        animateThemeChange(Duration.millis(350));

        if (accentColor != null) {
            getScene().getRoot().pseudoClassStateChanged(accentColor.pseudoClass(), false);
        }

        getScene().getRoot().pseudoClassStateChanged(color.pseudoClass(), true);
        this.accentColor = color;
        EVENT_BUS.publish(new ThemeEvent(EventType.COLOR_CHANGE));
    }

    public void resetAccentColor() {
        animateThemeChange(Duration.millis(350));

        if (accentColor != null) {
            getScene().getRoot().pseudoClassStateChanged(accentColor.pseudoClass(), false);
            accentColor = null;
        }

        EVENT_BUS.publish(new ThemeEvent(EventType.COLOR_CHANGE));
    }

    // Helper methods
    private void setCustomDeclaration(String property, String value) {
        customCSSDeclarations.put(property, value);
    }

    private void removeCustomDeclaration(String property) {
        customCSSDeclarations.remove(property);
    }

    private void setCustomRule(String selector, String rule) {
        customCSSRules.put(selector, rule);
    }

    private void animateThemeChange(Duration duration) {
        Image snapshot = scene.snapshot(null);
        Pane  root     = (Pane) scene.getRoot();

        ImageView imageView = new ImageView(snapshot);
        root.getChildren().add(imageView);

        var transition = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(imageView.opacityProperty(), 1, Interpolator.EASE_OUT)), new KeyFrame(duration, new KeyValue(imageView.opacityProperty(), 0, Interpolator.EASE_OUT)));

        transition.setOnFinished(e -> root.getChildren().remove(imageView));
        transition.play();
    }

    private void reloadCustomCSS() {
        Objects.requireNonNull(scene);
        StringBuilder css = new StringBuilder();

        css.append(".root:");
        css.append(USER_CUSTOM.getPseudoClassName());
        css.append(" {\n");
        customCSSDeclarations.forEach((k, v) -> {
            css.append("\t");
            css.append(k);
            css.append(": ");
            css.append(v);
            css.append(";\n");
        });
        css.append("}\n");

        customCSSRules.forEach((k, v) -> {
            css.append(".body:");
            css.append(USER_CUSTOM.getPseudoClassName());
            css.append(" ");
            css.append(k);
            css.append(" {");
            css.append(v);
            css.append("}\n");
        });

        getScene().getRoot().getStylesheets().removeIf(uri -> uri.startsWith("data:text/css"));
        getScene().getRoot()
                  .getStylesheets()
                  .add("data:text/css;base64," + Base64.getEncoder().encodeToString(css.toString().getBytes(UTF_8)));
        getScene().getRoot().pseudoClassStateChanged(USER_CUSTOM, true);
    }

    public void resetCustomCSS() {
        customCSSDeclarations.clear();
        customCSSRules.clear();
        getScene().getRoot().pseudoClassStateChanged(USER_CUSTOM, false);
    }

    private static class InstanceHolder {
        private static final ThemeManager INSTANCE;

        static {
            INSTANCE = new ThemeManager();
        }
    }
}
