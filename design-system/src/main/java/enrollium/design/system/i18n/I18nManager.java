package enrollium.design.system.i18n;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Slf4j
public class I18nManager {
    @Getter
    private static final I18nManager                   instance             = new I18nManager();
    private final        Map<Language, ResourceBundle> bundles              = new HashMap<>();
    private final        CompletableFuture<Void>       initializationFuture = new CompletableFuture<>();
    @Getter
    private              Language                      currentLanguage;

    private I18nManager() {
        try {
            // Load resource bundles for supported languages
            for (Language language : Language.values()) bundles.put(language, ResourceBundle.getBundle("i18n.messages_" + language.getCode()));

            // Validate bundles
            for (Map.Entry<Language, ResourceBundle> entry : bundles.entrySet()) {
                Language       language = entry.getKey();
                ResourceBundle bundle   = entry.getValue();

                for (TranslationKey key : TranslationKey.values()) {
                    try {
                        if (!bundle.containsKey(key.getKey()))
                            throw new MissingResourceException("Missing key in resource bundle", bundle.getClass()
                                                                                                       .getName(), key.getKey());
                    } catch (MissingResourceException e1) {
                        log.error("Language '{}' is missing the key: {} | {}", language.getCode(), key.getKey(), e1.getMessage());
                        throw new RuntimeException("Language '" + language.getCode() + "' is missing the key: " + key.getKey(), e1);
                    }
                }
            }
            log.info("All resource bundles validated successfully.");

            // Subscribe to language changes in SettingsManager
            SettingsManager.getInstance()
                           .observe(Setting.LANGUAGE)
                           .distinctUntilChanged()
                           .subscribe(lang -> currentLanguage = Language.fromAnything((String) lang), //
                                   error -> log.error("Failed to update language | {}", error.getMessage()));

            // Set initial language
            String initialLangCode = SettingsManager.getInstance().get(Setting.LANGUAGE);
            try {
                currentLanguage = Language.fromAnything(initialLangCode);
            } catch (IllegalArgumentException e) {
                log.error("Invalid initial language code: {}", initialLangCode);
                currentLanguage = Language.EN; // Fallback to English
            }

            initializationFuture.complete(null);
            log.info("I18nManager initialized successfully.");
        } catch (Exception e) {
            initializationFuture.completeExceptionally(e);
            log.error("Failed to initialize I18nManager | {}", e.getMessage());
            throw e;
        }
    }

    public static void BlockingInit() throws InterruptedException, ExecutionException {
        instance.initializationFuture.get();
    }

    public String get(TranslationKey key) {
        return bundles.getOrDefault(currentLanguage, bundles.get(Language.EN)).getString(key.getKey());
    }

    public Set<String> getAvailableLanguages() {
        return Arrays.stream(Language.values())
                     .map(Language::getDisplayName)
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
