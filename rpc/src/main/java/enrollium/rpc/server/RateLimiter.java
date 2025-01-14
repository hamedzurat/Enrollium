package enrollium.rpc.server;

import enrollium.rpc.core.JsonUtils;
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
    private static final    int                                MAX_REQUESTS_PER_MINUTE = 32;
    private static volatile RateLimiter                        instance;
    private final           ConcurrentHashMap<String, Integer> requestCounts           = new ConcurrentHashMap<>();
    private final           ScheduledExecutorService           resetLoop               = Executors.newSingleThreadScheduledExecutor();

    private RateLimiter() {
        log.info("Starting rate-limiter");

        // Reset counts every minute
        resetLoop.scheduleAtFixedRate(() -> {
            log.info("requestCounts: \n{}", JsonUtils.toPrettyJson(requestCounts));

            requestCounts.clear();
            log.info("Resting rate-limiter");
        }, 1, 1, TimeUnit.MINUTES);
    }

    public static RateLimiter getInstance() {
        if (instance == null) synchronized (RateLimiter.class) {
            if (instance == null) instance = new RateLimiter();
        }

        return instance;
    }

    /**
     * Checks if a request from the given IP should be allowed.
     */
    public boolean isRequestDenied(String identifier) {
        return requestCounts.merge(identifier, 1, Integer::sum) > MAX_REQUESTS_PER_MINUTE;
    }

    @Override
    public void close() {
        instance = null;
        resetLoop.shutdown();
        log.info("Shutdown rate-limiter");
    }
}
