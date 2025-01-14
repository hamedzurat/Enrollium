package enrollium.design.system.i18n;

import lombok.Getter;


public enum Language {
    EN("en", "English"), //
    BN("bn", "বাংলা"), //
    ;
    /*
     *
     */
    @Getter
    private final String code;
    @Getter
    private final String displayName;

    Language(String code, String displayName) {
        this.code        = code;
        this.displayName = displayName;
    }

    public static Language fromAnything(String code) {
        for (Language language : values())
            if (language.getCode().equalsIgnoreCase(code) || language.getDisplayName().equalsIgnoreCase(code))
                return language;

        throw new IllegalArgumentException("Unknown language code: " + code);
    }

    public static Language fromCode(String code) {
        for (Language language : values())
            if (language.getCode().equalsIgnoreCase(code)) return language;

        throw new IllegalArgumentException("Unknown language code: " + code);
    }

    public static Language fromDisplayName(String displayName) {
        for (Language language : values())
            if (language.getDisplayName().equalsIgnoreCase(displayName)) return language;

        throw new IllegalArgumentException("Unknown language display name: " + displayName);
    }
}
