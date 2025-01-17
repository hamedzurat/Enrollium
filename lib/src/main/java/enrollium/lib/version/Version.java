package enrollium.lib.version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;


/**
 * Utility class to access application version information.
 */
public final class Version {
    private static final String VERSION_FILE = "version.properties";
    private static final String VERSION_KEY  = "version";
    private static final String VERSION      = loadVersion();

    private Version() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    private static String loadVersion() {
        File versionFile = new File(Objects.requireNonNull(Version.class.getClassLoader().getResource(VERSION_FILE))
                                           .getFile());
        if (!versionFile.exists())
            throw new RuntimeException("Version file '" + VERSION_FILE + "' not found in classpath.");

        try (InputStream input = Version.class.getClassLoader().getResourceAsStream(VERSION_FILE)) {
            if (input == null) throw new RuntimeException("Version file '" + VERSION_FILE + "' could not be opened.");

            Properties properties = new Properties();
            properties.load(input);
            String version = properties.getProperty(VERSION_KEY);

            if (version == null || version.trim().isEmpty())
                throw new RuntimeException("Version key '" + VERSION_KEY + "' is missing or empty in '" + VERSION_FILE + "'.");

            return version.trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load version from '" + VERSION_FILE + "': " + e.getMessage(), e);
        }
    }

    /**
     * Returns the application version.
     *
     * @return the version string
     */
    public static String getVersion() {
        return VERSION;
    }
}
