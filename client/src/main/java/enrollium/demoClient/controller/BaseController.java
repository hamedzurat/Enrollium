package enrollium.demoClient.controller;

import enrollium.design.system.i18n.I18nManager;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;
import javafx.application.Platform;


public abstract class BaseController {
    protected final SettingsManager settings    = SettingsManager.getInstance();
    protected final I18nManager     i18nManager = I18nManager.getInstance();

    protected void initializeBase() {
        // Subscribe to language changes and trigger text updates
        settings.observe(Setting.LANGUAGE)
                .distinctUntilChanged()
                .subscribe(_ -> Platform.runLater(this::updateTexts));

        // Initial update of texts
        updateTexts();
    }

    /**
     * This method must be implemented by child controllers to update their texts.
     */
    protected abstract void updateTexts();
}
