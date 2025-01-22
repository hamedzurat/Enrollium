package enrollium.client.page.admin;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;


public class RegistrationStatus extends BasePage {
    public static final TranslationKey NAME = TranslationKey.RegistrationStatus;

    public RegistrationStatus() {
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
