package enrollium.lib.banner;

import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.stream.Stream;


/**
 * Utility class for system information reporting.
 */
public final class Issue {
    private static final String INDENT   = "    ";
    private static final String NEW_LINE = System.lineSeparator();
    private static final long   DIVISOR  = 1024L * 1024L * 1024L;

    private Issue() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Prints system information to the provided logger.
     *
     * @param log the logger to use for output
     *
     * @throws NullPointerException if log is null
     */
    public static void print(Logger log) {
        java.util.Objects.requireNonNull(log, "Logger cannot be null");

        log.info(formatSection("JAVA SYSTEM PROPERTIES:", getSystemProperties()));
        log.info(formatSection("ENVIRONMENT VARIABLES:", getEnvironmentVariables()));
        log.info(formatSection("HARDWARE INFORMATION:", getHardwareInfo()));
    }

    private static String formatSection(String title, Stream<Map.Entry<String, String>> content) {
        StringBuilder output = new StringBuilder(title).append(NEW_LINE);
        content.forEach(entry -> output.append(INDENT)
                                       .append(entry.getKey())
                                       .append(": ")
                                       .append(entry.getValue())
                                       .append(NEW_LINE));
        return output.toString();
    }

    private static Stream<Map.Entry<String, String>> getSystemProperties() {
        return System.getProperties()
                     .entrySet()
                     .stream()
                     .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue().toString()))
                     .sorted(Map.Entry.comparingByKey());
    }

    private static Stream<Map.Entry<String, String>> getEnvironmentVariables() {
        return System.getenv()
                     .entrySet()
                     .stream()
                     .map(entry -> Map.entry(entry.getKey(), entry.getValue()))
                     .sorted(Map.Entry.comparingByKey());
    }

    private static Stream<Map.Entry<String, String>> getHardwareInfo() {
        var osBean = ManagementFactory.getOperatingSystemMXBean();
        Stream<Map.Entry<String, String>> basicInfo = Stream.of( //
                Map.entry("OS Name", osBean.getName()), //
                Map.entry("OS Version", osBean.getVersion()), //
                Map.entry("OS Architecture", osBean.getArch()), //
                Map.entry("Available Processors (Cores)", String.valueOf(osBean.getAvailableProcessors())));

        return Stream.concat(basicInfo, getExtendedHardwareInfo(osBean));
    }

    private static Stream<Map.Entry<String, String>> getExtendedHardwareInfo(Object osBean) {
        try {
            var extendedBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return Stream.of( //
                    Map.entry("Total Physical Memory", formatMemory(extendedBean.getTotalMemorySize())), //
                    Map.entry("Free Physical Memory", formatMemory(extendedBean.getFreeMemorySize())), //
                    Map.entry("Total Swap Space", formatMemory(extendedBean.getTotalSwapSpaceSize())), //
                    Map.entry("Free Swap Space", formatMemory(extendedBean.getFreeSwapSpaceSize())));
        } catch (ClassCastException e) {
            return Stream.of(Map.entry("Extended Hardware Info", "Extended hardware details are not available on this JVM."));
        }
    }

    private static String formatMemory(long bytes) {
        return bytes / DIVISOR + " GB";
    }
}
