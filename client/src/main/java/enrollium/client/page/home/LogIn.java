package enrollium.client.page.home;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Message;
import atlantafx.base.controls.PasswordTextField;
import atlantafx.base.theme.Styles;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import enrollium.design.system.memory.Volatile;
import enrollium.rpc.client.ClientRPC;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.util.regex.Pattern;


@SuppressWarnings("UnnecessaryLambda")
public final class LogIn extends BasePage {
    public static final  TranslationKey    NAME          = TranslationKey.LOGIN;
    // https://emailregex.com/index.html
    private static final Pattern           EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])", Pattern.CASE_INSENSITIVE);
    private static final Volatile          memory        = Volatile.getInstance();
    private              CustomTextField   emailField;
    private              PasswordTextField passwordField;
    private              Button            loginButton, logoutButton;
    private Message statusMessage;

    public LogIn() {
        super();

        addPageHeader();
        addNode(loginPortal());
    }

    public Node loginPortal() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(0, //
                0.2 * Screen.getPrimary().getBounds().getWidth(), //
                0, //
                0.2 * Screen.getPrimary().getBounds().getWidth()) //
        );

        statusMessage = new Message("Welcome", "Please enter your credentials.", new FontIcon(Material2OutlinedAL.HELP_OUTLINE));
        statusMessage.getStyleClass().add(Styles.ACCENT);

        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add(Styles.TITLE_2);

        emailField = new CustomTextField();
        emailField.setPromptText("zurat@uiu.ac.bd");
        emailField.setPrefWidth(70);
        emailField.textProperty().addListener((_, _, newText) -> validateEmail(newText));
        emailField.getStyleClass().add(Styles.LARGE);

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add(Styles.TITLE_2);

        passwordField = new PasswordTextField();
        passwordField.setPromptText("*********");
        passwordField.setPrefWidth(70);
        passwordField.getStyleClass().add(Styles.LARGE);

        var icon = new FontIcon(Feather.EYE_OFF);
        icon.setCursor(Cursor.HAND);
        icon.setOnMouseClicked(_ -> togglePasswordVisibility(icon));
        icon.getStyleClass().add(Styles.LARGE);
        passwordField.setRight(icon);
        passwordField.textProperty().addListener((_, _, newText) -> evaluatePasswordStrength(newText));

        loginButton = new Button("Login", new FontIcon(Feather.LOG_IN));
        loginButton.getStyleClass().addAll(Styles.LARGE, Styles.ACCENT);
        loginButton.setMnemonicParsing(true);
        loginButton.setOnAction(_ -> performLogin());
        loginButton.setDisable(true);

        logoutButton = new Button("Logout", new FontIcon(Feather.LOG_OUT));
        logoutButton.getStyleClass().addAll(Styles.LARGE, Styles.DANGER);
        logoutButton.setMnemonicParsing(true);
        logoutButton.setOnAction(_ -> {
            ClientRPC.getInstance().logout();
            replaceStatusMessage(new Message("Success", "Logout Successful!", new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE)), Styles.SUCCESS);
            toggleButton();
        });
        logoutButton.setDisable(true);

        Button fillStudentButton = new Button("Fill demo Student");
        fillStudentButton.getStyleClass().addAll(Styles.LARGE, Styles.BUTTON_OUTLINED);
        fillStudentButton.setOnAction(e -> {
            emailField.setText("demo.student@uiu.ac.bd");
            passwordField.setText("demo$tudentP4ss");
        });

        Button fillFacultyButton = new Button("Fill demo Faculty");
        fillFacultyButton.getStyleClass().addAll(Styles.LARGE, Styles.BUTTON_OUTLINED);
        fillFacultyButton.setOnAction(e -> {
            emailField.setText("admin@uiu.ac.bd");
            passwordField.setText("demoAdm1nPa$$");
        });

        HBox buttonRow = new HBox(10, loginButton, logoutButton, fillStudentButton, fillFacultyButton);
        buttonRow.setPadding(new Insets(10, 0, 10, 0));
        toggleButton();

        emailField.textProperty().addListener((_, _, _) -> toggleButton());
        passwordField.textProperty().addListener((_, _, _) -> toggleButton());

        passwordField.textProperty().addListener((_, _, newText) -> {
            evaluatePasswordStrength(newText);
            String strength = getPasswordStrength(passwordField.getPassword());
            if (!newText.isEmpty()) passwordLabel.setText("Password (Strength: " + strength + ")");
            else passwordLabel.setText("Password");
        });

        container.getChildren().addAll(new Region() {{
                                           setPrefHeight(Screen.getPrimary().getBounds().getHeight() * 0.15);
                                       }}, //
                statusMessage, //
                new Region() {{
                    setPrefHeight(30);
                }}, //
                emailLabel, //
                emailField, //
                new Region() {{
                    setPrefHeight(30);
                }}, //
                passwordLabel, //
                passwordField, //
                new Region() {{
                    setPrefHeight(30);
                }}, //
                buttonRow);
        return container;
    }

    private boolean isAuth() {
        return ClientRPC.isAuthenticated();
    }

    private String getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) return "Weak";

        boolean hasUppercase   = password.matches(".*[A-Z].*");
        boolean hasLowercase   = password.matches(".*[a-z].*");
        boolean hasDigit       = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        int     length         = password.length();

        int strengthCount = 0;
        if (hasUppercase) strengthCount++;
        if (hasLowercase) strengthCount++;
        if (hasDigit) strengthCount++;
        if (hasSpecialChar) strengthCount++;

        if (length >= 12 && strengthCount == 4) return "Very Strong";
        else if (length >= 8 && strengthCount == 4) return "Strong";
        else if (length >= 8 && strengthCount >= 3) return "Medium";
        else return "Weak";
    }

    private void validateEmail(String email) {
        emailField.pseudoClassStateChanged(Styles.STATE_DANGER, !EMAIL_PATTERN.matcher(email)
                                                                              .matches() && !email.isEmpty());
    }

    private void togglePasswordVisibility(FontIcon icon) {
        passwordField.setRevealPassword(!passwordField.getRevealPassword());
        icon.setIconCode(passwordField.getRevealPassword() ? Feather.EYE_OFF : Feather.EYE);
    }

    private void evaluatePasswordStrength(String password) {
        passwordField.pseudoClassStateChanged(Styles.STATE_DANGER, password.length() < 8 && !password.isEmpty());
    }

    private void toggleButton() {
        boolean isEmailValid    = EMAIL_PATTERN.matcher(emailField.getText()).matches();
        boolean isPasswordValid = passwordField.getText().length() >= 8;
        loginButton.setDisable(!(isEmailValid && isPasswordValid && !isAuth()));
        logoutButton.setDisable(!isAuth());
    }

    private void performLogin() {
        loginButton.setDisable(true);
        replaceStatusMessage(new Message("Loading", "Connecting to server...", new FontIcon(Feather.LOADER)), Styles.WARNING);

        String emailInput    = emailField.getText();
        String passwordInput = passwordField.getPassword();

        ClientRPC.initialize(emailInput, passwordInput).subscribeOn(Schedulers.io()).subscribe(client -> {
            String type = switch (memory.get("auth_user_type").toString().toUpperCase()) {
                case "STUDENT" -> "Student";
                case "TEACHER" -> "Faculty";
                case "ADMIN" -> "System Administrator";
                default -> "Known User Type";
            };

            String message = String.format("""
                    Login Successful!
                    User Type: %s
                    Session Token: %s
                    User ID: %s
                    """, type, memory.get("auth_session_token"), memory.get("auth_user_id"));

            Platform.runLater(() -> {
                replaceStatusMessage(new Message("Success", message, new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE)), Styles.SUCCESS);
                toggleButton();
            });
        }, error -> {
            Platform.runLater(() -> {
                String errorMessage      = error.getMessage();
                String finalErrorMessage = "Unknown Error";

                if (errorMessage.contains("Connection refused")) finalErrorMessage = "Cannot connect to the server. Please check if the server is running.";
                else if (errorMessage.contains("User not found")) finalErrorMessage = "Email address not found. Please check your email.";
                else if (errorMessage.contains("Invalid password")) finalErrorMessage = "Incorrect password. Please try again.";
                else if (errorMessage.contains("Set email and password first")) finalErrorMessage = "Please enter your email and password.";
                else if (errorMessage.contains("Authentication failed")) finalErrorMessage = "Authentication failed. Please check your credentials and try again.";

                replaceStatusMessage(new Message("Error", "Login failed: " + finalErrorMessage, new FontIcon(Material2OutlinedAL.ERROR_OUTLINE)), Styles.DANGER);
                toggleButton();
            });
        });
    }

    private void replaceStatusMessage(Message newMessage, String style) {
        VBox parent = (VBox) statusMessage.getParent();
        int  index  = parent.getChildren().indexOf(statusMessage);
        newMessage.getStyleClass().add(style);
        parent.getChildren().set(index, newMessage);
        statusMessage = newMessage;
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    @Override
    protected void onRendered() {
        super.onRendered();
    }
}
