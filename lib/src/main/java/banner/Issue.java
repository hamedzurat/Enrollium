package banner;

import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Map;
import java.util.Properties;


public class Issue {
    public static void print(Logger log) {
        // Print Java System Properties
        log.info("JAVA SYSTEM PROPERTIES:");
        Properties properties = System.getProperties();
        properties.forEach((key, value) -> log.info("{}: {}", key, value));
        log.info("");

        // Print Environment Variables
        log.info("ENVIRONMENT VARIABLES:");
        Map<String, String> envVars = System.getenv();
        envVars.forEach((key, value) -> log.info("{}: {}", key, value));
        log.info("");

        // Print Hardware Info
        log.info("HARDWARE INFORMATION:");
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        // Check for optional methods (available only on some JVMs)
        log.info("OS Name: {}", osBean.getName());
        log.info("OS Version: {}", osBean.getVersion());
        log.info("OS Architecture: {}", osBean.getArch());
        log.info("Available Processors (Cores): {}", osBean.getAvailableProcessors());

        // Attempt to retrieve memory info (requires casting to com.sun.management.OperatingSystemMXBean)
        try {
            com.sun.management.OperatingSystemMXBean osMxBean = (com.sun.management.OperatingSystemMXBean) osBean;
            log.info("Total Physical Memory: {} MB", osMxBean.getTotalPhysicalMemorySize() / 1_024 / 1_024);
            log.info("Free Physical Memory: {} MB", osMxBean.getFreePhysicalMemorySize() / 1_024 / 1_024);
            log.info("Total Swap Space: {} MB", osMxBean.getTotalSwapSpaceSize() / 1_024 / 1_024);
            log.info("Free Swap Space: {} MB", osMxBean.getFreeSwapSpaceSize() / 1_024 / 1_024);
        } catch (ClassCastException e) {
            log.info("Extended hardware details are not available on this JVM.");
        }
    }
}
