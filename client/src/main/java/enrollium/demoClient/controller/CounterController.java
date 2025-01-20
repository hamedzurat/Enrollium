package enrollium.demoClient.controller;

import enrollium.design.system.i18n.I18nManager;
import enrollium.design.system.i18n.Language;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;


public class CounterController extends BaseController {
    private final SettingsManager  settings    = SettingsManager.getInstance();
    private final I18nManager      i18nManager = I18nManager.getInstance();
    //
    @FXML
    private       Label            helloText;
    @FXML
    private       Label            counterDisplay;
    @FXML
    private       Button           incrementButton;
    @FXML
    private       Button           decrementButton;
    @FXML
    private       Button           resetButton;
    @FXML
    private       ComboBox<String> languageDropdown;

    @FXML
    private void initialize() {
        // Initialize base functionality
        initializeBase();

        // Initialize counter display with current value
        Platform.runLater(() -> counterDisplay.setText(String.valueOf((Integer) settings.get(Setting.COUNTER))));

        // Subscribe to counter changes
        settings.observe(Setting.COUNTER)
                .distinctUntilChanged()
                .subscribe(value -> Platform.runLater(() -> counterDisplay.setText(String.valueOf(value))));

        // Initialize language dropdown
        languageDropdown.getItems().addAll(i18nManager.getAvailableLanguages());
        languageDropdown.setValue(i18nManager.getCurrentLanguage().getDisplayName());
        languageDropdown.setOnAction(_ -> onLanguageChange());
    }

    @Override
    protected void updateTexts() {
        helloText.setText(i18nManager.get(TranslationKey.HELLO));
        incrementButton.setText(i18nManager.get(TranslationKey.INCREMENT));
        decrementButton.setText(i18nManager.get(TranslationKey.DECREMENT));
        resetButton.setText(i18nManager.get(TranslationKey.RESET));
        languageDropdown.setPromptText(i18nManager.get(TranslationKey.SELECT_LANGUAGE));
    }

    @FXML
    private void onLanguageChange() {
        settings.set(Setting.LANGUAGE, Language.fromDisplayName(languageDropdown.getValue()).getCode());
    }

    @FXML
    private void onIncrementClick() {
        settings.set(Setting.COUNTER, (int) settings.get(Setting.COUNTER) + 1);
    }

    @FXML
    private void onDecrementClick() {
        settings.set(Setting.COUNTER, (int) settings.get(Setting.COUNTER) - 1);
    }

    @FXML
    private void onResetClick() {
        settings.set(Setting.COUNTER, 0);
    }
}
