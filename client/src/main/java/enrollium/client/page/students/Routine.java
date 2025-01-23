package enrollium.client.page.students;

import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;


public class Routine extends BasePage {
    public static final TranslationKey NAME = TranslationKey.Routine;

    public Routine() {
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
