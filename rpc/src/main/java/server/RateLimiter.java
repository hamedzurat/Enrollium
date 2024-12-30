package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RateLimiter {
    private static final Logger                             log           = LoggerFactory.getLogger(RateLimiter.class);
    private static final int                                MAX_REQUESTS  = 100;
    private final        ConcurrentHashMap<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final        ScheduledExecutorService           resettingLoop = Executors.newSingleThreadScheduledExecutor();

    public RateLimiter() {
        resettingLoop.scheduleAtFixedRate(() -> {
            log.debug("Resetting rate limiter counts");
            requestCounts.clear();
        }, 1, 1, TimeUnit.MINUTES);
    }

    public boolean allowRequest(String ipAddress) {
        return requestCounts.merge(ipAddress, 1, Integer::sum) <= MAX_REQUESTS;
    }

    public void shutdown() {
        resettingLoop.shutdown();
    }
}
