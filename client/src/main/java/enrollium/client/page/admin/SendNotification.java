package enrollium.client.page.admin;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;


public class SendNotification extends BasePage {
    public static final TranslationKey NAME = TranslationKey.SendNotification;

    public SendNotification() {
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
