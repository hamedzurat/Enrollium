package settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


@Slf4j
public class SettingsManager {
    private static final String                                APP_NAME             = "Enrollium";
    private static final ObjectMapper                          MAPPER               = new ObjectMapper();
    private static       SettingsManager                       instance;
    private final        Map<Setting, BehaviorSubject<Object>> subjects             = new ConcurrentHashMap<>();
    private final        Map<Setting, Object>                  settings             = new ConcurrentHashMap<>();
    private final        Path                                  configPath;
    private final        Path                                  settingsFile;
    private final        CompletableFuture<Void>               initializationFuture = new CompletableFuture<>();

    private SettingsManager() {
        this.configPath   = getConfigPath();
        this.settingsFile = configPath.resolve("settings.json");

        log.info("Config Location: {}", settingsFile);

        try {
            loadSettings();
            initializationFuture.complete(null);
        } catch (Exception e) {
            initializationFuture.completeExceptionally(e);
            throw e;
        }
    }

    public static synchronized SettingsManager getInstance() {
        if (instance == null) instance = new SettingsManager();
        return instance;
    }

    public static void BlockingInit() throws InterruptedException, ExecutionException {
        getInstance();
        instance.initializationFuture.get();
        log.info("SettingsManager initialized successfully.");
    }

    private Path getConfigPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String configPath;

        // Windows: %APPDATA%\Enrollium
        // macOS: ~/Library/Application Support/Enrollium
        // Linux/Unix: $XDG_CONFIG_HOME/enrollium or ~/.config/enrollium
        if (os.contains("win")) {
            configPath = System.getenv("APPDATA") + File.separator + APP_NAME;
        } else if (os.contains("mac")) configPath = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME;
        else {
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfig != null && !xdgConfig.isEmpty()) configPath = xdgConfig + File.separator + APP_NAME;
            else configPath = System.getProperty("user.home") + "/.config/" + APP_NAME;
        }

        return Paths.get(configPath);
    }

    private void loadSettings() {
        try {
            // Create config directory if it doesn't exist
            if (!Files.exists(configPath)) Files.createDirectories(configPath);

            // Create settings file if it doesn't exist
            if (!Files.exists(settingsFile)) {
                setDefaultsAndSave();
                return;
            }

            // Read and parse settings
            JsonNode root = MAPPER.readTree(settingsFile.toFile());

            // Load each setting
            for (Setting setting : Setting.values()) {
                JsonNode settingNode = root.get(setting.name());
                if (settingNode != null && settingNode.has("value") && settingNode.has("type")) {
                    String   type      = settingNode.get("type").asText();
                    JsonNode valueNode = settingNode.get("value");

                    // Validate type matches before loading
                    if (type.equals(setting.getType().getSimpleName())) {
                        Object value = parseValue(valueNode, setting);
                        if (value != null) {
                            settings.put(setting, value);
                            continue;
                        }
                    }
                }
                // If anything goes wrong, use default
                settings.put(setting, setting.getDefaultValue());
            }

            log.info("Settings loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load settings", e);
            setDefaultsAndSave();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T parseValue(JsonNode node, Setting setting) {
        try {
            if (setting.isType(Boolean.class)) return (T) Boolean.valueOf(node.asBoolean());
            else if (setting.isType(Integer.class)) return (T) Integer.valueOf(node.asInt());
            else if (setting.isType(Double.class)) return (T) Double.valueOf(node.asDouble());
            else if (setting.isType(String.class)) return (T) node.asText();
        } catch (Exception e) {
            log.warn("Failed to parse value for setting: {}", setting.name(), e);
        }
        return null;
    }

    private void saveSettings() {
        try {
            ObjectNode root = MAPPER.createObjectNode();

            settings.forEach((setting, value) -> {
                ObjectNode settingNode = root.putObject(setting.name());
                settingNode.put("type", setting.getType().getSimpleName());

                if (value instanceof Boolean) settingNode.put("value", (Boolean) value);
                else if (value instanceof Integer) settingNode.put("value", (Integer) value);
                else if (value instanceof Double) settingNode.put("value", (Double) value);
                else if (value instanceof String) settingNode.put("value", (String) value);
            });

            MAPPER.writerWithDefaultPrettyPrinter().writeValue(settingsFile.toFile(), root);

            log.info("Settings saved successfully");
        } catch (Exception e) {
            log.error("Failed to save settings", e);
        }
    }

    public <T> void set(Setting setting, T value) {
        if (!setting.getType().isInstance(value))
            throw new IllegalArgumentException("Invalid type for setting " + setting.name() + //
                                               ". Expected " + setting.getType().getSimpleName() + //
                                               " but got " + value.getClass().getSimpleName());

        settings.put(setting, value);

        BehaviorSubject<Object> subject = subjects.computeIfAbsent(setting, _ -> BehaviorSubject.createDefault(value));

        subject.onNext(value);
        saveSettings();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Setting setting) {
        return (T) settings.getOrDefault(setting, setting.getDefaultValue());
    }

    @SuppressWarnings("unchecked")
    public <T> Observable<T> observe(Setting setting) {
        if (!settings.containsKey(setting)) set(setting, setting.getDefaultValue());

        BehaviorSubject<Object> subject = subjects.computeIfAbsent(setting, k -> BehaviorSubject.createDefault(settings.get(k)));

        return (Observable<T>) subject;
    }

    public void resetToDefaults() {
        setDefaultsAndSave();
    }

    private void setDefaultsAndSave() {
        settings.clear();
        subjects.clear();

        for (Setting setting : Setting.values()) settings.put(setting, setting.getDefaultValue());

        saveSettings();
    }

    public void deleteSettingsFile() {
        try {
            Files.deleteIfExists(settingsFile);
            log.info("Settings file deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete settings file", e);
        }
    }
}
