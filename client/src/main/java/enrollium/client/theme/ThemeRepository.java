package enrollium.client.theme;

import atlantafx.base.theme.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.ThemeEvent;
import enrollium.client.event.ThemeEvent.EventType;
import enrollium.design.system.settings.Setting;
import enrollium.design.system.settings.SettingsManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;


// +-------------------------------------------+
// |           ThemeRepository                 |
// |  +-------------------------------------+  |
// |  |  Built-in Themes (internalThemes)   |  |
// |  |  - PrimerLight, NordDark, Dracula   |  |
// |  +-------------------------------------+  |
// |  +-------------------------------------+  |
// |  |  User-Added Themes (externalThemes) |  |
// |  |  - Custom CSS Themes                |  |
// |  +-------------------------------------+  |
// |  |  Preferences (Persistent Storage)   |  |
// |  +-------------------------------------+  |
// |  |  Event Publishing on Add/Remove     |  |
// +-------------------------------------------+
//
// Acts as a central repository for all UI themes in the application.
public final class ThemeRepository {
    private static final ObjectMapper             MAPPER           = new ObjectMapper();
    private static final Comparator<SamplerTheme> THEME_COMPARATOR = Comparator.comparing(SamplerTheme::getName);
    private final        List<SamplerTheme>       internalThemes   = Arrays.asList(new SamplerTheme(new PrimerLight()), new SamplerTheme(new PrimerDark()), new SamplerTheme(new NordLight()), new SamplerTheme(new NordDark()), new SamplerTheme(new CupertinoLight()), new SamplerTheme(new CupertinoDark()), new SamplerTheme(new Dracula()));
    private final        List<SamplerTheme>       externalThemes   = new ArrayList<>();
    private final        SettingsManager          settingsManager;

    public ThemeRepository() {
        this.settingsManager = SettingsManager.getInstance();
    }

    public List<SamplerTheme> getAll() {
        var list = new ArrayList<>(internalThemes);
        list.addAll(externalThemes);
        return list;
    }

    public SamplerTheme addFromFile(File file) {
        Objects.requireNonNull(file);

        if (!isFileValid(file.toPath())) {
            throw new RuntimeException("Invalid CSS file \"" + file.getAbsolutePath() + "\".");
        }

        String filename = file.getName();
        String themeName = Arrays.stream(filename.replace(".css", "").split("[-_]"))
                                 .map(s -> !s.isEmpty() ? s.substring(0, 1).toUpperCase() + s.substring(1) : "")
                                 .collect(Collectors.joining(" "));

        var theme = new SamplerTheme(Theme.of(themeName, file.toString(), filename.contains("dark")));

        if (!isUnique(theme)) {
            throw new RuntimeException("A theme with the same name or user agent stylesheet already exists.");
        }

        externalThemes.add(theme);
        externalThemes.sort(THEME_COMPARATOR);

        // Persist external themes
        persistExternalThemes();

        DefaultEventBus.getInstance().publish(new ThemeEvent(EventType.THEME_ADD));

        return theme;
    }

    public void remove(SamplerTheme theme) {
        Objects.requireNonNull(theme);
        externalThemes.removeIf(t -> Objects.equals(t.getName(), theme.getName()));

        // Persist updated external themes list
        persistExternalThemes();

        DefaultEventBus.getInstance().publish(new ThemeEvent(EventType.THEME_REMOVE));
    }

    private void persistExternalThemes() {
        try {
            List<String> themePaths = externalThemes.stream()
                                                    .map(theme -> theme.getResource().toPath().toString())
                                                    .collect(Collectors.toList());

            String json = MAPPER.writeValueAsString(themePaths);
            settingsManager.set(Setting.EXTERNAL_THEMES, json);
        } catch (JsonProcessingException e) {
            // Log error but don't throw to maintain functionality
        }
    }

    public boolean isFileValid(Path path) {
        Objects.requireNonNull(path);
        return !Files.isDirectory(path, NOFOLLOW_LINKS) && Files.isRegularFile(path, NOFOLLOW_LINKS) && Files.isReadable(path) && path.getFileName()
                                                                                                                                      .toString()
                                                                                                                                      .endsWith(".css");
    }

    public boolean isUnique(SamplerTheme theme) {
        Objects.requireNonNull(theme);
        for (SamplerTheme t : getAll()) {
            if (Objects.equals(t.getName(), theme.getName()) || Objects.equals(t.getPath(), theme.getPath())) {
                return false;
            }
        }
        return true;
    }
}
