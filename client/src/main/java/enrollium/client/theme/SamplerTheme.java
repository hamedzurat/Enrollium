package enrollium.client.theme;

import atlantafx.base.theme.Theme;
import enrollium.client.FileResource;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enrollium.client.theme.ThemeManager.APP_STYLESHEETS;
import static enrollium.client.theme.ThemeManager.PROJECT_THEMES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;


// +------------------------------------------------+
// |              SamplerTheme                      |
// |  +------------------------------------------+  |
// |  |      Built-in Themes (Project)           |  |
// |  |      - PrimerLight, Dracula              |  |
// |  +------------------------------------------+  |
// |  |      External Themes (User Imported)     |  |
// |  |      - Custom CSS Files                  |  |
// |  +------------------------------------------+  |
// |  |   Color Parsing for UI Customization     |  |
// |  |   Hot-reloading with File Monitoring     |  |
// +------------------------------------------------+
/*
JavaFX has two ways to apply styles:
 - Scene.setUserAgentStylesheet() → Dynamic and observable but lacks full control.
 - Application.setUserAgentStylesheet() → Static and non-observable.

JavaFX also struggles with hot-reloading CSS, meaning changes to stylesheets aren't reflected in real-time.
SamplerTheme works around this by:
 - Wrapping themes for better CSS management.
 - Supporting live reloading of styles.
 - Managing both built-in and external themes.
*/
public final class SamplerTheme implements Theme {
    private static final int                 PARSE_LIMIT         = 250;
    private static final Pattern             COLOR_PATTERN_REGEX = Pattern.compile(" *?(-color-(fg|bg|accent|success|danger|warning)-.+?): *?(.+?);");
    private final        Theme               theme;
    private              FileTime            lastModified;
    private              Map<String, String> colors;

    public SamplerTheme(Theme theme) {
        Objects.requireNonNull(theme);

        // Prevents wrapping a SamplerTheme inside another.
        if (theme instanceof SamplerTheme)
            throw new IllegalArgumentException("Sampler theme must not be wrapped into itself.");

        this.theme = theme;
    }

    @Override
    public String getName() {
        return theme.getName();
    }

    // Application.setUserAgentStylesheet() only accepts URL (or URL string representation),
    // any external file path must have "file://" prefix
    @Override
    public String getUserAgentStylesheet() {
        return getResource().toURI().toString();
    }

    @Override
    public @Nullable String getUserAgentStylesheetBSS() {
        return theme.getUserAgentStylesheetBSS();
    }

    @Override
    public boolean isDarkMode() {
        return theme.isDarkMode();
    }

    // The core app stylesheets used alongside the theme.
    public Set<String> getAllStylesheets() {
        return Set.of(APP_STYLESHEETS);
    }

    // Checks whether wrapped theme is a project theme or user external theme.
    public boolean isProjectTheme() {
        return PROJECT_THEMES.contains(theme.getClass());
    }

    // Tries to parse theme CSS and extract conventional looked-up colors. There are few limitations:
    // - minified CSS files are not supported
    // - only first PARSE_LIMIT lines will be read
    public Map<String, String> parseColors() throws IOException {
        FileResource file = getResource();
        return file.internal() ? parseColorsForClasspath(file) : parseColorsForFilesystem(file);
    }

    private Map<String, String> parseColors(BufferedReader br) throws IOException {
        Map<String, String> colors = new HashMap<>();

        String line;
        int    lineCount = 0;

        while ((line = br.readLine()) != null) {
            Matcher matcher = COLOR_PATTERN_REGEX.matcher(line);
            if (matcher.matches()) {
                colors.put(matcher.group(1), matcher.group(3));
            }

            lineCount++;
            if (lineCount > PARSE_LIMIT) {
                break;
            }
        }

        return colors;
    }

    private Map<String, String> parseColorsForClasspath(FileResource file) throws IOException {
        // classpath resources are static, no need to parse project theme more than once
        if (colors != null) return colors;

        try (var br = new BufferedReader(new InputStreamReader(file.getInputStream(), UTF_8))) {
            colors = parseColors(br);
        }

        return colors;
    }

    // Avoids re-parsing unchanged CSS files by checking the last modified time.
    private Map<String, String> parseColorsForFilesystem(FileResource file) throws IOException {
        // return cached colors if file wasn't changed since the last read
        FileTime fileTime = Files.getLastModifiedTime(file.toPath(), NOFOLLOW_LINKS);
        if (Objects.equals(fileTime, lastModified)) return colors;

        try (var br = new BufferedReader(new InputStreamReader(file.getInputStream(), UTF_8))) {
            colors = parseColors(br);
        }

        // don't save time before parsing is finished to avoid
        // remembering operation that might end up with an error
        lastModified = fileTime;

        return colors;
    }

    public String getPath() {
        return getResource().toPath().toString();
    }

    public FileResource getResource() {
        if (!isProjectTheme()) return FileResource.createExternal(theme.getUserAgentStylesheet());
        return FileResource.createInternal(theme.getUserAgentStylesheet(), Theme.class);
    }

    public Theme unwrap() {
        return theme;
    }

    @SafeVarargs
    private <T> Set<T> merge(T first, T... arr) {
        var set = new LinkedHashSet<T>();
        set.add(first);
        Collections.addAll(set, arr);
        return set;
    }
}
