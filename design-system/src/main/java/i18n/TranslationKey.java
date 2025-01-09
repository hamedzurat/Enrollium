package i18n;

import lombok.Getter;


@Getter
public enum TranslationKey {
    HELLO("hello"), //
    INCREMENT("increment"), //
    DECREMENT("decrement"), //
    RESET("reset"), //
    SELECT_LANGUAGE("select_language"), //
    ;
    /*
     *
     */
    private final String key;

    TranslationKey(String key) {
        this.key = key;
    }
}
