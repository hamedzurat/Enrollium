package version;

import java.io.IOException;
import java.io.InputStream;
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
        try (InputStream input = Version.class.getClassLoader().getResourceAsStream(VERSION_FILE)) {
            if (input == null) return "Unknown";
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty(VERSION_KEY, "Unknown");
        } catch (IOException e) {
            return "Unknown";
        }
    }

    /**
     * Returns the application version.
     *
     * @return the version string, or "Unknown" if version cannot be determined
     */
    public static String getVersion() {
        return VERSION;
    }
}
