package version;

import java.io.InputStream;
import java.util.Properties;


public class Version {
    private static final String VERSION;

    static {
        Properties properties = new Properties();
        try (InputStream input = Version.class.getClassLoader().getResourceAsStream("version.properties")) {
            properties.load(input);
            VERSION = properties.getProperty("version", "Unknown");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load version", e);
        }
    }

    public static String getVersion() {
        return VERSION;
    }
}
