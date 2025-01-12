package enrollium.client.theme;

import atlantafx.base.theme.*;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.ThemeEvent;
import enrollium.client.event.ThemeEvent.EventType;

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
    // A comparator to sort themes alphabetically by their name.
    private static final Comparator<SamplerTheme> THEME_COMPARATOR = Comparator.comparing(SamplerTheme::getName);
    private final        List<SamplerTheme>       internalThemes   = Arrays.asList(new SamplerTheme(new PrimerLight()), //
                                                                                   new SamplerTheme(new PrimerDark()), //
                                                                                   new SamplerTheme(new NordLight()), //
                                                                                   new SamplerTheme(new NordDark()), //
                                                                                   new SamplerTheme(new CupertinoLight()), //
                                                                                   new SamplerTheme(new CupertinoDark()), //
                                                                                   new SamplerTheme(new Dracula()) //
    );
    private final        List<SamplerTheme>       externalThemes   = new ArrayList<>();

    public ThemeRepository() {}

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

        // creating GUI dialogs is hard, so we just obtain theme name from the file name :)
        String filename = file.getName();
        String themeName = Arrays
                .stream(filename.replace(".css", "").split("[-_]"))
                .map(s -> !s.isEmpty()
                          ? s
                                    .substring(0, 1)
                                    .toUpperCase() + s.substring(1)
                          : "")
                .collect(Collectors.joining(" "));

        var theme = new SamplerTheme(Theme.of(themeName, file.toString(), filename.contains("dark")));

        if (!isUnique(theme)) {
            throw new RuntimeException("A theme with the same name or user agent stylesheet already exists in the repository.");
        }

        externalThemes.add(theme);
        externalThemes.sort(THEME_COMPARATOR);
        DefaultEventBus.getInstance().publish(new ThemeEvent(EventType.THEME_ADD));

        return theme;
    }

    public void remove(SamplerTheme theme) {
        Objects.requireNonNull(theme);
        externalThemes.removeIf(t -> Objects.equals(t.getName(), theme.getName()));
        DefaultEventBus.getInstance().publish(new ThemeEvent(EventType.THEME_REMOVE));
    }

    public boolean isFileValid(Path path) {
        Objects.requireNonNull(path);
        return !Files.isDirectory(path, NOFOLLOW_LINKS) && Files.isRegularFile(path, NOFOLLOW_LINKS) && Files.isReadable(path) && path
                .getFileName()
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
