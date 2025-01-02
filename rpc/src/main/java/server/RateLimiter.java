package server;

import core.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Simple rate limiter that tracks requests per IP address.
 */
@Slf4j
public class RateLimiter implements AutoCloseable {
    private static final int                                MAX_REQUESTS_PER_MINUTE = 10;
    private final        ConcurrentHashMap<String, Integer> requestCounts           = new ConcurrentHashMap<>();
    private final        ScheduledExecutorService           resetLoop               = Executors.newSingleThreadScheduledExecutor();

    public RateLimiter() {
        // Reset counts every minute
        resetLoop.scheduleAtFixedRate(() -> {
            log.info("requestCounts: \n{}", JsonUtils.toPrettyJson(requestCounts));

            requestCounts.clear();
            log.info("Resting rate-limiter");
        }, 1, 1, TimeUnit.MINUTES);

        log.info("Starting rate-limiter");
    }

    /**
     * Checks if a request from the given IP should be allowed.
     */
    public boolean allowRequest(String ipAddress) {
        return requestCounts.merge(ipAddress, 1, Integer::sum) <= MAX_REQUESTS_PER_MINUTE;
    }

    @Override
    public void close() {
        resetLoop.shutdown();
        log.info("Shutdown rate-limiter");
    }
}
