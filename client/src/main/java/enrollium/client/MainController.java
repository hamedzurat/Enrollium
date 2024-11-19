package enrollium.client;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class MainController {
    private final PublishSubject<Integer> clickSubject = PublishSubject.create();
    private final CompositeDisposable     disposables  = new CompositeDisposable();
    @FXML
    private       Label                   counterLabel;
    @FXML
    private       Button                  incrementButton;

    @FXML
    public void initialize() {
        // Set up the counter observable
        Observable<Integer> counterObservable = clickSubject.scan(0, (count, value) -> count + 1)
                                                            .distinctUntilChanged() // only change if new
                                                            .doOnError(error -> Platform.runLater(() -> {
                                                                counterLabel.setText("Error");
                                                            }))
                                                            .retry() // resubscribes after errors
                                                            .share(); // ot and sharable observable

        // Subscribe to counter updates
        disposables.add( // for cleaning subscriptions to prevent memory
                counterObservable.subscribe( // say what will happen when THE button is clicked.
                        count -> Platform.runLater(() -> {
                            counterLabel.setText(String.valueOf(count));
                            System.out.println(count);
                        }), //
                        error -> System.err.println("Error in counter stream: " + error) //
                ));

        // Connect button clicks to the subject
        incrementButton.setOnAction(event -> clickSubject.onNext(1)); // trigger

        // Set up cleanup when window closes
        Platform.runLater(() -> {
            incrementButton.getScene().getWindow().setOnCloseRequest(event -> {
                if (!disposables.isDisposed()) {
                    disposables.dispose();
                }
            });
        });
    }
}
