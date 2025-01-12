package enrollium.client.page.general;

import atlantafx.base.theme.Styles;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.ThemeEvent;
import enrollium.client.page.OutlinePage;
import enrollium.client.theme.SamplerTheme;
import enrollium.client.theme.ThemeManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

import java.util.Objects;

import static enrollium.client.event.ThemeEvent.EventType;
import static enrollium.client.theme.ThemeManager.DEFAULT_FONT_SIZE;


@SuppressWarnings("UnnecessaryLambda")
public final class ThemePage extends OutlinePage {
    public static final  String                       NAME              = "Home";
    private static final ThemeManager                 TM                = ThemeManager.getInstance();
    private static final String                       DEFAULT_FONT_ID   = "Default";
    private final        ReadOnlyObjectWrapper<Color> bgBaseColor       = new ReadOnlyObjectWrapper<>(Color.WHITE);
    private final        ChoiceBox<SamplerTheme>      themeSelector     = createThemeSelector();
    private final        ComboBox<String>             fontFamilyChooser = createFontFamilyChooser();
    private final        Spinner<Integer>             fontSizeSpinner   = createFontSizeSpinner();

    public ThemePage() {
        super();

        DefaultEventBus.getInstance().subscribe(ThemeEvent.class, e -> {
            var eventType = e.getEventType();
            if (eventType == EventType.THEME_ADD || eventType == EventType.THEME_REMOVE) {
                themeSelector.getItems().setAll(TM.getRepository().getAll());
                selectCurrentTheme();
            }
            if (eventType == EventType.THEME_CHANGE || eventType == EventType.COLOR_CHANGE) {
                fontFamilyChooser.getSelectionModel().select(DEFAULT_FONT_ID);
                fontSizeSpinner.getValueFactory().setValue(DEFAULT_FONT_SIZE);
            }
        });

        // mandatory base bg for flatten color calc
        Styles.appendStyle(this, "-fx-background-color", "-color-bg-default");
        backgroundProperty().addListener((obs, old, val) -> bgBaseColor.set(val != null && !val.getFills().isEmpty()
                                                                            ? (Color) val.getFills().get(0).getFill()
                                                                            : Color.WHITE));

        addPageHeader();
        addNode(createThemeManagementSection());

        Platform.runLater(this::selectCurrentTheme);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void onRendered() {
        super.onRendered();
    }

    private Node createThemeManagementSection() {
        var accentSelector = new AccentColorSelector();

        // ~

        var grid = new GridPane();
        grid.setHgap(HGAP_20);
        grid.setVgap(VGAP_10);
        grid.addRow(0, new Label("Color theme"), themeSelector);
        grid.addRow(1, new Label("Accent color"), accentSelector);
        grid.addRow(2, new Label("Font"), new HBox(10, fontFamilyChooser, fontSizeSpinner));

        return grid;
    }

    private ChoiceBox<SamplerTheme> createThemeSelector() {
        var choiceBox = new ChoiceBox<SamplerTheme>();

        var themes = TM.getRepository().getAll();
        choiceBox.getItems().setAll(themes);

        // set initial value
        var currentTheme = Objects.requireNonNullElse(TM.getTheme(), TM.getDefaultTheme());
        themes
                .stream()
                .filter(t -> Objects.equals(currentTheme.getName(), t.getName()))
                .findFirst()
                .ifPresent(t -> choiceBox.getSelectionModel().select(t));

        // must be after setting the initial value
        choiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null && getScene() != null) {
                TM.setTheme(val);
            }
        });
        choiceBox.setPrefWidth(310);

        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SamplerTheme theme) {
                return theme != null ? theme.getName() : "";
            }

            @Override
            public SamplerTheme fromString(String themeName) {
                return TM
                        .getRepository()
                        .getAll()
                        .stream()
                        .filter(t -> Objects.equals(themeName, t.getName()))
                        .findFirst()
                        .orElse(null);
            }
        });

        return choiceBox;
    }

    private ComboBox<String> createFontFamilyChooser() {
        var comboBox = new ComboBox<String>();
        comboBox.setPrefWidth(200);

        // keyword to reset font family to its default value
        comboBox.getItems().add(DEFAULT_FONT_ID);
        comboBox.getItems().addAll(FXCollections.observableArrayList(Font.getFamilies()));

        // select active font family value on page load
        comboBox.getSelectionModel().select(TM.getFontFamily());
        comboBox.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                TM.setFontFamily(DEFAULT_FONT_ID.equals(val) ? ThemeManager.DEFAULT_FONT_FAMILY_NAME : val);
            }
        });

        return comboBox;
    }

    private Spinner<Integer> createFontSizeSpinner() {
        var spinner = new Spinner<Integer>(ThemeManager.SUPPORTED_FONT_SIZE.get(0), ThemeManager.SUPPORTED_FONT_SIZE.get(ThemeManager.SUPPORTED_FONT_SIZE.size() - 1), TM.getFontSize());
        spinner.setPrefWidth(100);

        // Instead of this we should obtain font size from a rendered node.
        // But since it's not trivial (thanks to JavaFX doesn't expose relevant API)
        // we just keep current font size inside ThemeManager singleton.
        // It works fine if ThemeManager default font size value matches
        // default theme font size value.
        spinner.getValueFactory().setValue(TM.getFontSize());

        spinner.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                TM.setFontSize(val);
            }
        });

        return spinner;
    }

    private void selectCurrentTheme() {
        if (TM.getTheme() != null) {
            themeSelector
                    .getItems()
                    .stream()
                    .filter(t -> Objects.equals(TM.getTheme().getName(), t.getName()))
                    .findFirst()
                    .ifPresent(t -> themeSelector.getSelectionModel().select(t));
        }
    }
}
