package enrollium.client.page.students;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;


public class RequestWithdraw extends BasePage {
    public static final TranslationKey NAME = TranslationKey.RequestWithdraw;

    public RequestWithdraw() {
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
