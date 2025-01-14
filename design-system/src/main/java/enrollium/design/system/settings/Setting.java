package enrollium.design.system.settings;

import enrollium.design.system.i18n.Language;
import lombok.Getter;

import java.util.Arrays;


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

    public static boolean validateValue(Setting setting, Object value) {
        if (value == null) return false;

        if (setting.isType(Integer.class)) {
            int intValue = (Integer) value;
            switch (setting) {
                case FONT_SIZE:
                    return intValue >= 2 && intValue <= 128;
                case COUNTER:
                    return true;
            }
        } else if (setting.isType(String.class)) {
            String strValue = (String) value;
            switch (setting) {
                case LANGUAGE:
                    return Arrays.stream(Language.values()).map(Language::getCode).toList().contains(value);
                case FONT_FAMILY:
                    return !strValue.isEmpty() && strValue.length() <= 50;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue() {
        return (T) defaultValue;
    }

    public boolean isType(Class<?> clazz) {
        return type.equals(clazz);
    }
}
