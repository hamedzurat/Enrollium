package enrollium.client.page.home;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.I18nManager;
import enrollium.design.system.i18n.TranslationKey;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;


public class SignUp extends BasePage {
    public static final  TranslationKey NAME        = TranslationKey.SignUp;
    private static final I18nManager    i18nManager = I18nManager.getInstance();
    private              VBox           container;
    private              Message        message;

    public SignUp() {
        super();
        addPageHeader();
        setupUI();
    }

    private void setupUI() {
        container = new VBox();
        container.setAlignment(Pos.CENTER);
        VBox.setVgrow(container, Priority.ALWAYS);

        createMessage();
        addNode(container);
    }

    private void createMessage() {
        // Remove the existing message to avoid duplication
        if (message != null) container.getChildren().remove(message);

        message = new Message(i18nManager.get(TranslationKey.SignUp_title), i18nManager.get(TranslationKey.SignUp_desc), new FontIcon(Material2OutlinedAL.EMAIL));
        message.getStyleClass().addAll(Styles.ACCENT, Styles.TITLE_4, Styles.BORDERED, Styles.LARGE, Styles.ELEVATED_4);

        // Bind the message width to 50% of the container's width
        message.maxWidthProperty().bind(container.widthProperty().multiply(0.5));

        // Add the new message only if it's not already added
        if (!container.getChildren().contains(message)) container.getChildren().add(message);
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
        Platform.runLater(this::createMessage);
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
