package enrollium.design.system.i18n;

import lombok.Getter;


@Getter
public enum TranslationKey {
    HELLO("hello"), //
    INCREMENT("increment"), //
    DECREMENT("decrement"), //
    RESET("reset"), //
    SELECT_LANGUAGE("select_language"), //
    LOGIN("login"),
    SECTION("section"),
    SPACETIME("space_time"),
    BUTTON("button"),
    CHANGEPASSWORD("change_pass"),
    USERPAGE("user_page"),
    ADVISING("advising"),
    ;
    /*
     *
     */
    private final String key;

    TranslationKey(String key) {
        this.key = key;
    }
}
