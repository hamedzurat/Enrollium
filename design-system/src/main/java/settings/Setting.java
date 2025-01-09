package settings;

import lombok.Getter;


public enum Setting {
    DARK_MODE(Boolean.class, true),
    FONT_SIZE(Integer.class, 12),
    FONT_FAMILY(String.class, "Noto Sans"),
    LANGUAGE(String.class, "en"),
    COUNTER(Integer.class, 0);
    /*
     *
     */
    @Getter
    private final Class<?> type;
    private final Object   defaultValue;

    Setting(Class<?> type, Object defaultValue) {
        this.type         = type;
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue() {
        return (T) defaultValue;
    }

    public boolean isType(Class<?> clazz) {
        return type.equals(clazz);
    }
}
