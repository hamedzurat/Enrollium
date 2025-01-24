package enrollium.client.page.home;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


@SuppressWarnings("UnnecessaryLambda")
public class ForgotPassword extends BasePage {
    public static final  TranslationKey           NAME          = TranslationKey.ForgotPassword;
    private static final Pattern                  EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])", Pattern.CASE_INSENSITIVE);
    private final        ScheduledExecutorService scheduler     = Executors.newSingleThreadScheduledExecutor();
    private              CustomTextField          emailField;
    private              Button                   sendResetButton;
    private              Message                  statusMessage;

    public ForgotPassword() {
        super();

        addPageHeader();
        addNode(forgotPasswordPortal());
    }

    public Node forgotPasswordPortal() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(0, 200, 0, 200));

        statusMessage = new Message("If you forgot your password", "Enter your email to reset your password.", new FontIcon(Material2OutlinedAL.INFO));
        statusMessage.getStyleClass().add(Styles.ACCENT);

        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add(Styles.TITLE_2);

        emailField = new CustomTextField();
        emailField.setPromptText("zarif.demo@uiu.ac.bd");
        emailField.textProperty().addListener((_, _, newText) -> validateEmail(newText));
        emailField.getStyleClass().add(Styles.LARGE);

        sendResetButton = new Button("Send Reset Link", new FontIcon(Feather.SEND));
        sendResetButton.getStyleClass().addAll(Styles.LARGE, Styles.ACCENT);
        sendResetButton.setOnAction(_ -> sendResetEmail());
        sendResetButton.setDisable(true);

        Button fillDemoEmailButton = new Button("Fill Demo Email");
        fillDemoEmailButton.getStyleClass().addAll(Styles.LARGE, Styles.BUTTON_OUTLINED);
        fillDemoEmailButton.setOnAction(_ -> emailField.setText("demo.user@uiu.ac.bd"));

        emailField.textProperty().addListener((_, _, _) -> toggleSendButton());

        // Place buttons in a horizontal row
        HBox buttonRow = new HBox(10, sendResetButton, fillDemoEmailButton);

        container.getChildren().addAll(new Region() {{setPrefHeight(50);}}, statusMessage, new Region() {{
            setPrefHeight(30);
        }}, emailLabel, emailField, new Region() {{setPrefHeight(30);}}, buttonRow);

        return container;
    }

    private void validateEmail(String email) {
        emailField.pseudoClassStateChanged(Styles.STATE_DANGER, !EMAIL_PATTERN.matcher(email)
                                                                              .matches() && !email.isEmpty());
    }

    private void toggleSendButton() {
        sendResetButton.setDisable(!EMAIL_PATTERN.matcher(emailField.getText()).matches());
    }

    private void sendResetEmail() {
        sendResetButton.setDisable(true);
        replaceStatusMessage(new Message("Loading", "Sending reset link...", new FontIcon(Feather.LOADER)), Styles.WARNING);

        // Simulate server processing delay
        scheduler.schedule(() -> {
            Platform.runLater(() -> {
                replaceStatusMessage(new Message("Success", "A reset link has been sent to " + emailField.getText() + ". Follow the instruction from it.", new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE)), Styles.SUCCESS);
                sendResetButton.setDisable(false);
            });
        }, RANDOM.nextInt(500, 1000), TimeUnit.MILLISECONDS);
    }

    private void replaceStatusMessage(Message newMessage, String style) {
        VBox parent = (VBox) statusMessage.getParent();
        int  index  = parent.getChildren().indexOf(statusMessage);
        newMessage.getStyleClass().add(style);
        parent.getChildren().set(index, newMessage);
        statusMessage = newMessage;
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
