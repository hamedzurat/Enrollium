package enrollium.client.page.home;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;


public class SignUp extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SignUp;

    public SignUp() {
        super();

        addPageHeader();
        addFormattedText(TranslationKey.DEV);
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}
