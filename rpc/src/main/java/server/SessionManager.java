package server;

import core.*;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Manages active RPC sessions, handling creation, validation, and cleanup.
 */
@Slf4j
public class SessionManager implements AutoCloseable {
    private static final long                                   HEARTBEAT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long                                   CLEANUP_INTERVAL_MS  = TimeUnit.MINUTES.toMillis(1);
    private final        SecureRandom                           secureRandom         = new SecureRandom();
    private final        ConcurrentHashMap<String, SessionInfo> sessions             = new ConcurrentHashMap<>();
    private final        ScheduledExecutorService               cleanupExecutor      = Executors.newSingleThreadScheduledExecutor();
    private final        MessageHandler                         defaultMessageHandler;

    public SessionManager(MessageHandler defaultMessageHandler) {
        this.defaultMessageHandler = defaultMessageHandler;
        startCleanupTask();
    }

    /**
     * Creates a new session for a user.
     */
    public SessionInfo createSession(String userId, Socket socket, MessageHandler customHandler) {
        try {
            String         token   = generateSessionToken();
            MessageHandler handler = customHandler != null ? customHandler : defaultMessageHandler;

            RPCConnection connection = new RPCConnection(token, socket, handler);
            SessionInfo   session    = new SessionInfo(token, userId, connection);
            sessions.put(token, session);

            log.info("Created session for user: {} with token: {}", userId, token);
            return session;
        } catch (Exception e) {
            log.error("Failed to create session for user: {}", userId, e);
            throw new RuntimeException("Session creation failed", e);
        }
    }

    /**
     * Generates a unique session token.
     */
    private String generateSessionToken() {
        String token;
        do {
            token = secureRandom.ints(64, 0, 10).mapToObj(String::valueOf).collect(Collectors.joining());
        } while (sessions.containsKey(token));
        return token;
    }

    /**
     * Validates session existence and expiration.
     */
    public boolean validateSession(String token) {
        SessionInfo session = sessions.get(token);
        return session == null || !session.isActive() || session.isExpired();
    }

    /**
     * Updates session heartbeat.
     */
    public void updateHeartbeat(String token) {
        SessionInfo session = sessions.get(token);
        if (session != null) session.updateHeartbeat();
        log.info("Heartbeat from {}", session.getSessionToken());
    }

    /**
     * Adds tags to a session for grouping/filtering.
     */
    public void addSessionTags(String token, String... tags) {
        SessionInfo session = sessions.get(token);
        if (session != null) session.getTags().addAll(Arrays.asList(tags));
    }

    /**
     * Gets sessions matching all specified tags.
     */
    public Set<SessionInfo> getSessionsByTags(Collection<String> tags) {
        return sessions.values()
                       .stream()
                       .filter(session -> session.isActive() && session.getTags().containsAll(tags))
                       .collect(Collectors.toSet());
    }

    /**
     * Sends a request through a specific session.
     */
    public Single<Response> sendRequest(String token, Request request) {
        SessionInfo session = sessions.get(token);
        if (session == null || !session.isActive())
            return Single.error(new IllegalStateException("Invalid or inactive session"));

        return session.sendRequest(request);
    }

    /**
     * Broadcasts a request to all sessions with matching tags.
     */
    public void broadcast(Request request, Collection<String> tags) {
        Set<SessionInfo> targetSessions = getSessionsByTags(tags);
        for (SessionInfo session : targetSessions) {
            try {
                session.sendRequest(request)
                       .subscribe(response -> log.debug("Broadcast response received from session: {}", session.getSessionToken()), error -> log.error("Broadcast failed for session: {}", session.getSessionToken(), error));
            } catch (Exception e) {
                log.error("Error broadcasting to session: {}", session.getSessionToken(), e);
            }
        }
    }

    /**
     * Gets session info if available.
     */
    public Optional<SessionInfo> getSession(String token) {
        return Optional.ofNullable(sessions.get(token));
    }

    /**
     * Gets all active sessions.
     */
    public Collection<SessionInfo> getActiveSessions() {
        return sessions.values().stream().filter(SessionInfo::isActive).collect(Collectors.toList());
    }

    /**
     * Starts the periodic cleanup task.
     */
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupSessions, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
        log.info("Starting session manager");
    }

    /**
     * Cleans up expired and inactive sessions.
     */
    private void cleanupSessions() {
        log.info("Cleaning up SessionManager");

        sessions.entrySet().removeIf(entry -> {
            SessionInfo session         = entry.getValue();
            boolean     expired         = session.isExpired();
            boolean     heartbeatMissed = session.hasHeartbeatTimeout(HEARTBEAT_TIMEOUT_MS);
            boolean     inactive        = !session.isActive();

            if (expired || heartbeatMissed || inactive) {
                log.info("Removing session: {} (expired: {}, heartbeat missed: {}, inactive: {})", session.getSessionToken(), expired, heartbeatMissed, inactive);
                session.close();
                return true;
            }
            return false;
        });
    }

    /**
     * Removes a specific session.
     */
    public void removeSession(String token) {
        SessionInfo session = sessions.remove(token);
        if (session != null) {
            session.close();
            log.info("Removed session: {}", token);
        }
    }

    @Override
    public void close() {
        cleanupExecutor.shutdown();
        sessions.values().forEach(SessionInfo::close);
        sessions.clear();
        log.info("SessionManager shutdown complete");
    }
}
