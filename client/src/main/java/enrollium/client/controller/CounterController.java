package enrollium.client.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class CounterController {
    private final BehaviorSubject<Integer> counterSubject = BehaviorSubject.createDefault(0);
    @FXML
    private       Label                    counterDisplay;
    @FXML
    private       MFXButton                incrementButton;
    @FXML
    private       MFXButton                decrementButton;
    @FXML
    private       MFXButton                resetButton;

    @FXML
    private void initialize() {
        counterSubject.distinctUntilChanged().map(String::valueOf).subscribe(counterDisplay::setText);
    }

    @FXML
    private void onIncrementClick() {
        counterSubject.onNext(counterSubject.getValue() + 1);
    }

    @FXML
    private void onDecrementClick() {
        counterSubject.onNext(counterSubject.getValue() - 1);
    }

    @FXML
    private void onResetClick() {
        counterSubject.onNext(0);
    }
}
