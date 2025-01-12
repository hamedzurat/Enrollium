package enrollium.client.page.debug;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.BBCodeParser;
import enrollium.client.page.ExampleBox;
import enrollium.client.page.OutlinePage;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;


public final class Button extends OutlinePage {
    public static final String NAME = "Button";

    public Button() {
        super();

        addPageHeader();
        addFormattedText("""
                                 A simple button control. The button control can contain text and/or a graphic.
                                 A button control has three different modes:

                                 [ul]
                                 [li]Normal: A normal push button.[/li]
                                 [li]Default: A default button is the button that receives a keyboard [code]VK_ENTER[/code] press,
                                 if no other node in the scene consumes it.[/li]
                                 [li]Cancel: A cancel button is the button that receives a keyboard [code]VK_ESC[/code] press,
                                 if no other node in the scene consumes it.[/li][/ul]""");
        addSection("Usage", buttonUsageExample());
        addSection("Colored", coloredButtonExample());
        addSection("Icon Button", iconButtonExample());
        addSection("Circular", circularButtonExample());
        addSection("Outlined", outlinedButtonExample());
        addSection("Rounded", roundedButtonExample());
        addSection("Button Size", buttonSizeExample());
        addSection("Custom Styles", buttonCustomColorExample());
    }

    @Override
    public String getName() {
        return NAME;
    }

    private ExampleBox buttonUsageExample() {
        var normalBtn = new javafx.scene.control.Button("_Normal");
        normalBtn.setMnemonicParsing(true);

        var defaultBtn = new javafx.scene.control.Button("_Default");
        defaultBtn.setDefaultButton(true);
        defaultBtn.setMnemonicParsing(true);

        var outlinedBtn = new javafx.scene.control.Button("Out_lined");
        outlinedBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        outlinedBtn.setMnemonicParsing(true);

        var flatBtn = new javafx.scene.control.Button("_Flat");
        flatBtn.getStyleClass().add(Styles.FLAT);

        var box = new HBox(HGAP_20, normalBtn, defaultBtn, outlinedBtn, flatBtn);
        var description = BBCodeParser.createFormattedText("""
                                                                   The [i]Button[/i] comes with four CSS variants: normal (default), colored, \
                                                                   outlined, and flat (or text). To change the appearance of the [i]Button[/i], \
                                                                   you set the corresponding style classes that work as modifiers.""");
        return new ExampleBox(box, description);
    }

    private ExampleBox coloredButtonExample() {

        var accentBtn = new javafx.scene.control.Button("_Accent");
        accentBtn.getStyleClass().add(Styles.ACCENT);
        accentBtn.setMnemonicParsing(true);

        var successBtn = new javafx.scene.control.Button("_Success", new FontIcon(Feather.CHECK));
        successBtn.getStyleClass().add(Styles.SUCCESS);
        successBtn.setMnemonicParsing(true);

        var dangerBtn = new javafx.scene.control.Button("Da_nger", new FontIcon(Feather.TRASH));
        dangerBtn.getStyleClass().add(Styles.DANGER);
        dangerBtn.setContentDisplay(ContentDisplay.RIGHT);
        dangerBtn.setMnemonicParsing(true);

        // ~
        var accentOutBtn = new javafx.scene.control.Button("Accen_t");
        accentOutBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        accentOutBtn.setMnemonicParsing(true);

        var successOutBtn = new javafx.scene.control.Button("S_uccess", new FontIcon(Feather.CHECK));
        successOutBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SUCCESS);
        successOutBtn.setMnemonicParsing(true);

        var dangerOutBtn = new javafx.scene.control.Button("Dan_ger", new FontIcon(Feather.TRASH));
        dangerOutBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        dangerOutBtn.setContentDisplay(ContentDisplay.RIGHT);
        dangerOutBtn.setMnemonicParsing(true);

        // ~
        var accentFlatBtn = new javafx.scene.control.Button("Accen_t");
        accentFlatBtn.getStyleClass().addAll(Styles.FLAT, Styles.ACCENT);
        accentFlatBtn.setMnemonicParsing(true);

        var successFlatBtn = new javafx.scene.control.Button("S_uccess", new FontIcon(Feather.CHECK));
        successFlatBtn.getStyleClass().addAll(Styles.FLAT, Styles.SUCCESS);
        successFlatBtn.setMnemonicParsing(true);

        var dangerFlatBtn = new javafx.scene.control.Button("Dan_ger", new FontIcon(Feather.TRASH));
        dangerFlatBtn.getStyleClass().addAll(Styles.FLAT, Styles.DANGER);
        dangerFlatBtn.setContentDisplay(ContentDisplay.RIGHT);
        dangerFlatBtn.setMnemonicParsing(true);

        var box = new VBox(VGAP_20, new HBox(HGAP_20, accentBtn, successBtn, dangerBtn), new HBox(HGAP_20, accentOutBtn, successOutBtn, dangerOutBtn), new HBox(HGAP_20, accentFlatBtn, successFlatBtn, dangerFlatBtn));
        var description = BBCodeParser.createFormattedText("""
                                                                   You can change the [i]Button[/i] color simply by using predefined style class modifiers.""");

        return new ExampleBox(box, description);
    }

    private ExampleBox iconButtonExample() {

        var normalBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.MORE_HORIZONTAL));
        normalBtn.getStyleClass().addAll(Styles.BUTTON_ICON);

        var accentBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.MENU));
        accentBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT);

        var successBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.CHECK));
        successBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.SUCCESS);

        var dangerBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.TRASH));
        dangerBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_OUTLINED, Styles.DANGER);

        var flatAccentBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.MIC));
        flatAccentBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.ACCENT);

        var flatSuccessBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.USER));
        flatSuccessBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.SUCCESS);

        var flatDangerBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.CROSSHAIR));
        flatDangerBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);

        var box = new HBox(HGAP_20, normalBtn, accentBtn, successBtn, dangerBtn, flatAccentBtn, flatSuccessBtn, flatDangerBtn);
        var description = BBCodeParser.createFormattedText("""
                                                                   Icon buttons are present in two variants. The first one is just a \
                                                                   normal [i]Button[/i] but with no text and the second one is a flat button - \
                                                                   suitable for toolbars and similar controls.""");

        return new ExampleBox(box, description);
    }

    private ExampleBox circularButtonExample() {

        var normalBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.MORE_HORIZONTAL));
        normalBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE);

        var accentBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.MENU));
        accentBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.ACCENT);

        var successBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.CHECK));
        successBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.SUCCESS);

        var dangerBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.TRASH));
        dangerBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.BUTTON_OUTLINED, Styles.DANGER);

        var flatAccentBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.MIC));
        flatAccentBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.ACCENT);

        var flatSuccessBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.USER));
        flatSuccessBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.SUCCESS);

        var flatDangerBtn = new javafx.scene.control.Button(null, new FontIcon(Feather.CROSSHAIR));
        flatDangerBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.DANGER);

        var box = new HBox(HGAP_20, normalBtn, accentBtn, successBtn, dangerBtn, flatAccentBtn, flatSuccessBtn, flatDangerBtn);

        var description = BBCodeParser.createFormattedText("""
                                                                   You can also apply the [code]setShape()[/code] method to make the \
                                                                   [i]Button[/i] look circular.""");

        return new ExampleBox(box, description);
    }

    private ExampleBox outlinedButtonExample() {

        var accentOutBtn = new javafx.scene.control.Button("Accen_t");
        accentOutBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        accentOutBtn.setMnemonicParsing(true);

        var successOutBtn = new javafx.scene.control.Button("S_uccess", new FontIcon(Feather.CHECK));
        successOutBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SUCCESS);
        successOutBtn.setMnemonicParsing(true);

        var dangerOutBtn = new javafx.scene.control.Button("Dan_ger", new FontIcon(Feather.TRASH));
        dangerOutBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        dangerOutBtn.setContentDisplay(ContentDisplay.RIGHT);
        dangerOutBtn.setMnemonicParsing(true);

        var box = new HBox(HGAP_20, accentOutBtn, successOutBtn, dangerOutBtn);
        var description = BBCodeParser.createFormattedText("""
                                                                   Outlined buttons are medium-emphasis buttons. They contain actions that are \
                                                                   important but aren't the primary action in an app.""");

        return new ExampleBox(box, description);
    }

    private ExampleBox roundedButtonExample() {

        var normalBtn = new javafx.scene.control.Button("Normal");
        normalBtn.getStyleClass().addAll(Styles.SMALL, Styles.ROUNDED);

        var accentBtn = new javafx.scene.control.Button("Accent");
        accentBtn.getStyleClass().addAll(Styles.ROUNDED, Styles.ACCENT);

        var successBtn = new javafx.scene.control.Button("Success", new FontIcon(Feather.CHECK));
        successBtn.getStyleClass().addAll(Styles.LARGE, Styles.ROUNDED, Styles.BUTTON_OUTLINED, Styles.SUCCESS);

        var box = new HBox(HGAP_20, normalBtn, accentBtn, successBtn);
        box.setAlignment(Pos.CENTER_LEFT);

        var description = BBCodeParser.createFormattedText("""
                                                                   [i]Button[/i] corners can be rounded with the [code]Styles.ROUNDED[/code] \
                                                                   style class modifier.""");

        return new ExampleBox(box, description);
    }

    private ExampleBox buttonSizeExample() {

        var smallBtn = new javafx.scene.control.Button("Small");
        smallBtn.getStyleClass().addAll(Styles.SMALL);

        var normalBtn = new javafx.scene.control.Button("Normal");

        var largeBtn = new javafx.scene.control.Button("Large");
        largeBtn.getStyleClass().addAll(Styles.LARGE);

        var box = new HBox(HGAP_20, smallBtn, normalBtn, largeBtn);
        box.setAlignment(Pos.CENTER_LEFT);

        var description = BBCodeParser.createFormattedText("""
                                                                   For larger or smaller buttons, use the [code]Styles.SMALL[/code] or \
                                                                   [code]Styles.LARGE[/code] style classes, respectively.""");

        return new ExampleBox(box, description);
    }

    private ExampleBox buttonCustomColorExample() {

        var btn = new javafx.scene.control.Button("DO SOMETHING!");
        btn.getStyleClass().addAll(Styles.SUCCESS, Styles.LARGE);
        btn.setStyle("""
                             -color-button-bg: linear-gradient(
                                 to bottom right, -color-success-emphasis, darkblue
                             );
                             -color-button-bg-hover:   -color-button-bg;
                             -color-button-bg-focused: -color-button-bg;
                             -color-button-bg-pressed: -color-button-bg;""");

        var iconBtn = new javafx.scene.control.Button(null, new FontIcon(Material2AL.FAVORITE));
        iconBtn.getStyleClass().addAll("favorite-button", Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.DANGER);
        iconBtn.getStylesheets().add(Styles.toDataURI("""
                                                              .favorite-button.button >.ikonli-font-icon {
                                                                  -fx-fill: linear-gradient(
                                                                      to bottom right, pink, -color-danger-emphasis
                                                                  );
                                                                  -fx-icon-color: linear-gradient(
                                                                      to bottom right, pink, -color-danger-emphasis
                                                                  );
                                                                  -fx-font-size:  32px;
                                                                  -fx-icon-size:  32px;
                                                              }"""));

        var box = new HBox(HGAP_20, btn, iconBtn);
        box.setAlignment(Pos.CENTER_LEFT);

        var description = BBCodeParser.createFormattedText("""
                                                                   In addition to using the predefined [i]Button[/i] colors, you can add custom ones \
                                                                   by manipulating the looked-up color variables.""");

        return new ExampleBox(box, description);
    }
}
