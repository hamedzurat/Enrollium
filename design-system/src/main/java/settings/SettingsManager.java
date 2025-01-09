package settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import version.Version;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class SettingsManager {
    private static final String                                APP_NAME             = "Enrollium";
    private static final ObjectMapper                          MAPPER               = new ObjectMapper();
    private static final String                                CURRENT_VERSION      = Version.getVersion();
    private static       SettingsManager                       instance;
    private final        Map<Setting, BehaviorSubject<Object>> subjects             = new ConcurrentHashMap<>();
    private final        Map<Setting, Object>                  settings             = new ConcurrentHashMap<>();
    private final        Path                                  configPath;
    private final        Path                                  settingsFile;
    private final        CompletableFuture<Void>               initializationFuture = new CompletableFuture<>();
    private final        AtomicBoolean                         pendingSave          = new AtomicBoolean(false);
    private final        ScheduledExecutorService              executor             = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "SettingsManager-Saver");
        thread.setDaemon(true);
        return thread;
    });
    private final    AtomicInteger changeCount  = new AtomicInteger(0);
    @Getter
    private volatile long          lastModified = System.currentTimeMillis();

    private SettingsManager() {
        this.configPath   = getConfigPath();
        this.settingsFile = configPath.resolve("settings.json");

        log.info("Config Location: {}", settingsFile);

        try {
            loadSettings();
            initializationFuture.complete(null);
        } catch (Exception e) {
            log.error("Failed to initialize settings", e);
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
        if (os.contains("win")) configPath = System.getenv("APPDATA") + File.separator + APP_NAME;
        else if (os.contains("mac")) configPath = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME;
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

            // Version check
            String fileVersion = root.has("version") ? root.get("version").toString() : null;
            if (!CURRENT_VERSION.equals(fileVersion)) {
                log.info("Settings version mismatch. Expected: {}, Found: {}. Resetting to defaults.", CURRENT_VERSION, fileVersion);
                setDefaultsAndSave();
                return;
            }

            JsonNode settingsNode = root.get("settings");
            if (settingsNode == null) {
                log.warn("Invalid settings file format. Resetting to defaults.");
                setDefaultsAndSave();
                return;
            }

            for (Setting setting : Setting.values()) {
                JsonNode settingNode = settingsNode.get(setting.name());
                if (settingNode != null && settingNode.has("value")) {
                    Object value = parseValue(settingNode.get("value"), setting);
                    if (Setting.validateValue(setting, value)) {
                        settings.put(setting, value);
                        continue;
                    }
                }
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
            if (setting.isType(Integer.class)) return (T) Integer.valueOf(node.asInt());
            if (setting.isType(Double.class)) return (T) Double.valueOf(node.asDouble());
            if (setting.isType(String.class)) return (T) node.asText();
        } catch (Exception e) {
            log.warn("Failed to parse value for setting: {}", setting.name(), e);
        }
        return null;
    }

    private void saveSettings() {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("version", CURRENT_VERSION);

            ObjectNode settingsNode = root.putObject("settings");
            settings.forEach((setting, value) -> {
                ObjectNode settingNode = settingsNode.putObject(setting.name());
                if (value instanceof Boolean) settingNode.put("value", (Boolean) value);
                else if (value instanceof Integer) settingNode.put("value", (Integer) value);
                else if (value instanceof Double) settingNode.put("value", (Double) value);
                else if (value instanceof String) settingNode.put("value", (String) value);
            });

            MAPPER.writerWithDefaultPrettyPrinter().writeValue(settingsFile.toFile(), root);
            lastModified = System.currentTimeMillis();

            log.debug("Settings saved successfully. Total changes: {}", changeCount);
        } catch (Exception e) {
            log.error("Failed to save settings", e);
        }
    }

    public <T> void set(Setting setting, T value) {
        if (!setting.getType().isInstance(value)) {
            log.error("Invalid type for setting {}. Expected {} but got {}", setting.name(), setting.getType()
                                                                                                    .getSimpleName(), value.getClass()
                                                                                                                           .getSimpleName());
            return;
        }

        if (!Setting.validateValue(setting, value)) {
            log.error("Invalid value for setting {}: {}", setting.name(), value);
            return;
        }

        settings.put(setting, value);
        changeCount.incrementAndGet();

        BehaviorSubject<Object> subject = subjects.computeIfAbsent(setting, _ -> BehaviorSubject.createDefault(value));
        subject.onNext(value);

        if (pendingSave.compareAndSet(false, true)) {
            executor.schedule(() -> {
                pendingSave.set(false);
                saveSettings();
            }, 1, TimeUnit.SECONDS);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Setting setting) {
        return (T) settings.getOrDefault(setting, setting.getDefaultValue());
    }

    @SuppressWarnings("unchecked")
    public <T> Observable<T> observe(Setting setting) {
        if (!settings.containsKey(setting)) {
            set(setting, setting.getDefaultValue());
        }

        BehaviorSubject<Object> subject = subjects.computeIfAbsent(setting, k -> BehaviorSubject.createDefault(settings.get(k)));
        return (Observable<T>) subject;
    }

    public void resetToDefaults() {
        setDefaultsAndSave();
    }

    private void setDefaultsAndSave() {
        settings.clear();
        subjects.clear();
        changeCount.set(0);

        for (Setting setting : Setting.values()) {
            settings.put(setting, setting.getDefaultValue());
        }

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

    public int getChangeCount() {
        return changeCount.get();
    }

    public void shutdown() {
        try {
            if (pendingSave.get()) {
                saveSettings();
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
