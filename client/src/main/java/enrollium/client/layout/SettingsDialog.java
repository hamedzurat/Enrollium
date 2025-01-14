package enrollium.client.layout;

import enrollium.client.theme.SamplerTheme;
import enrollium.client.theme.ThemeManager;
import enrollium.design.system.i18n.I18nManager;
import enrollium.design.system.i18n.Language;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Objects;

import static enrollium.client.page.Page.HGAP_20;
import static enrollium.client.page.Page.VGAP_10;


final class SettingsDialog extends ModalDialog {
    private static final ThemeManager     TM                = ThemeManager.getInstance();
    private static final String           DEFAULT_FONT_ID   = "Default";
    private final        SettingsManager  settings          = SettingsManager.getInstance();
    private final        I18nManager      i18nManager       = I18nManager.getInstance();
    private final        TilePane         thumbnailsPane    = new TilePane(20, 20);
    private final        ToggleGroup      thumbnailsGroup   = new ToggleGroup();
    private final        ComboBox<String> fontFamilyChooser = createFontFamilyChooser();
    private final        Spinner<Integer> fontSizeSpinner   = createFontSizeSpinner();
    private final ColorPicker accentColorPicker = createAccentColorPicker();    private final ComboBox<String> languageDropdown = createLanguageDropdown();
    public SettingsDialog() {
        super();

        setId("theme-dialog");
        header.setTitle("Settings");
        content.setBody(createContent());
        content.setFooter(null);

        initializeThemeThumbnails();
    }

    private void initializeThemeThumbnails() {
        thumbnailsPane.getChildren().clear();
        TM.getRepository().getAll().forEach(theme1 -> {
            var thumbnail = new ThemeThumbnail(theme1);
            thumbnail.setToggleGroup(thumbnailsGroup);
            thumbnail.setUserData(theme1);
            thumbnail.setSelected(Objects.equals(TM.getTheme().getName(), theme1.getName()));
            thumbnailsPane.getChildren().add(thumbnail);
        });

        thumbnailsGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (val != null && val.getUserData() instanceof SamplerTheme theme) {
                settings.set(Setting.THEME, theme.getName());
            }
        });
    }

    private ComboBox<String> createFontFamilyChooser() {
        var comboBox = new ComboBox<String>();
        comboBox.setPrefWidth(200);
        comboBox.getItems().add(DEFAULT_FONT_ID);
        comboBox.getItems().addAll(FXCollections.observableArrayList(Font.getFamilies()));
        comboBox.getSelectionModel().select(TM.getFontFamily());

        comboBox.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                settings.set(Setting.FONT_FAMILY, DEFAULT_FONT_ID.equals(val) ? "Inter" : val);
            }
        });
        return comboBox;
    }

    private Spinner<Integer> createFontSizeSpinner() {
        Spinner<Integer> spinner = new Spinner<>(ThemeManager.SUPPORTED_FONT_SIZE.getFirst(), ThemeManager.SUPPORTED_FONT_SIZE.getLast(), TM.getFontSize());
        spinner.setPrefWidth(100);
        spinner.valueProperty().addListener((obs, old, val) -> {
            if (val != null) settings.set(Setting.FONT_SIZE, val);
        });
        return spinner;
    }

    private ColorPicker createAccentColorPicker() {
        var colorPicker = new ColorPicker(TM.getAccentColor() != null
                                          ? TM.getAccentColor().primaryColor()
                                          : Color.valueOf("#FFFFFF"));
        colorPicker.setOnAction(e -> settings.set(Setting.ACCENT_COLOR, colorPicker.getValue().toString()));

        return colorPicker;
    }

    private ComboBox<String> createLanguageDropdown() {
        ComboBox<String> dropdown = new ComboBox<>();
        dropdown.getItems().addAll(i18nManager.getAvailableLanguages());
        dropdown.setValue(i18nManager.getCurrentLanguage().getDisplayName());
        dropdown.setOnAction(_ -> settings.set(Setting.LANGUAGE, Language.fromDisplayName(languageDropdown.getValue())
                                                                         .getCode()));
        return dropdown;
    }

    private VBox createContent() {
        thumbnailsPane.setAlignment(Pos.TOP_CENTER);
        thumbnailsPane.setPrefColumns(3);
        thumbnailsPane.setStyle("-color-thumbnail-border:-color-border-subtle;");

        var grid = new GridPane();
        grid.setHgap(HGAP_20);
        grid.setVgap(VGAP_10);
        grid.addRow(0, new Label("Color theme:"), thumbnailsPane);
        grid.addRow(1, new Label("Accent color:"), accentColorPicker);
        grid.addRow(2, new Label("Font:"), new HBox(10, fontFamilyChooser, fontSizeSpinner));
        grid.addRow(3, new Label("Language:"), languageDropdown);

        var root = new VBox(grid);
        root.setPadding(new Insets(20));
        return root;
    }


}
