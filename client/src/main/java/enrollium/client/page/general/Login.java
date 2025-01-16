package enrollium.client.page.general;

import enrollium.client.page.OutlinePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;


@SuppressWarnings("UnnecessaryLambda")
public final class Login extends OutlinePage {
    public static final TranslationKey NAME = TranslationKey.LOGIN;

    public Login() {
        super();

        addPageHeader();
        addNode(loginPortal());
    }

    @Override
    protected void updateTexts() {

    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    @Override
    protected void onRendered() {
        super.onRendered();
    }

    private Node loginPortal() {
        return new GridPane();
    }
}
