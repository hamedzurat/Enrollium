package enrollium.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import settings.Setting;
import settings.SettingsManager;


public class CounterController {
    private final SettingsManager settings = SettingsManager.getInstance();
    @FXML
    private       Label           counterDisplay;
    @FXML
    private       Button          incrementButton;
    @FXML
    private       Button          decrementButton;
    @FXML
    private       Button          resetButton;

    @FXML
    private void initialize() {
        // Initialize counter display with current value
        Platform.runLater(() -> counterDisplay.setText(String.valueOf((Integer) settings.get(Setting.COUNTER))));

        // Subscribe to counter changes
        settings.observe(Setting.COUNTER)
                .distinctUntilChanged()
                .subscribe(value -> Platform.runLater(() -> counterDisplay.setText(String.valueOf(value))));
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
