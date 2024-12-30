package server;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class SessionManager {
    private static final Logger                                 log                   = LoggerFactory.getLogger(SessionManager.class);
    private static final long                                   SESSION_TIMEOUT_MS    = TimeUnit.DAYS.toMillis(1);
    private static final long                                   HEARTBEAT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30);
    private final        SecureRandom                           secureRandom          = new SecureRandom();
    private final        ConcurrentHashMap<String, SessionInfo> sessions              = new ConcurrentHashMap<>();
    private final        ScheduledExecutorService               cleanupLoop           = Executors.newSingleThreadScheduledExecutor();

    public SessionManager() {
        cleanupLoop.scheduleAtFixedRate(this::cleanupSessions, 1, 1, TimeUnit.MINUTES);
    }

    public String createSession(String userId, ServerConnection connection) {
        String      token   = generateSessionToken();
        SessionInfo session = new SessionInfo(token, userId, connection);
        sessions.put(token, session);
        log.info("Created session for user: {}", userId);
        return token;
    }

    private String generateSessionToken() {
        StringBuilder token = new StringBuilder();
        do {
            token.setLength(0); // https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder
            for (int i = 0; i < 64; i++) token.append(secureRandom.nextInt(10));
        } while (sessions.containsKey(token.toString()));

        return token.toString();
    }

    public boolean validateSession(String token) {
        SessionInfo session = sessions.get(token);
        if (session == null) return false;

        return System.currentTimeMillis() < session.getExpirationTime();
    }

    public void updateHeartbeat(String token) {
        SessionInfo session = sessions.get(token);
        if (session != null) session.setLastHeartbeat(System.currentTimeMillis());
    }

    public ServerConnection getConnection(String token) {
        SessionInfo session = sessions.get(token);
        return session != null ? session.getConnection() : null;
    }

    public void addTags(String token, String... tags) {
        SessionInfo session = sessions.get(token);
        if (session != null)
            session.getTags().addAll(Arrays.asList(tags));
    }

    public Set<String> getSessionsByTags(Collection<String> tags) {
        return sessions.entrySet()
                       .stream()
                       .filter(entry -> entry.getValue().getTags().containsAll(tags))
                       .map(Map.Entry::getKey)
                       .collect(Collectors.toSet());
    }

    private void cleanupSessions() {
        long currentTime = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            SessionInfo session         = entry.getValue();
            boolean     expired         = currentTime > session.getExpirationTime();
            boolean     heartbeatMissed = (currentTime - session.getLastHeartbeat()) > HEARTBEAT_INTERVAL_MS * 2;

            if (expired || heartbeatMissed) {
                log.info("Removing session for user: {} (expired: {}, heartbeat missed: {})", session.getUserId(), expired, heartbeatMissed);
                return true;
            }
            return false;
        });
    }

    public void removeSession(String token) {
        sessions.remove(token);
    }

    public void shutdown() {
        cleanupLoop.shutdown();
    }

    @Data
    public static class SessionInfo {
        private final String           sessionToken;
        private final String           userId;
        private final Set<String>      tags;
        private final ServerConnection connection;
        private       long             lastHeartbeat;
        private       long             expirationTime;

        public SessionInfo(String sessionToken, String userId, ServerConnection connection) {
            this.sessionToken   = sessionToken;
            this.userId         = userId;
            this.connection     = connection;
            this.tags           = ConcurrentHashMap.newKeySet();
            this.lastHeartbeat  = System.currentTimeMillis();
            this.expirationTime = this.lastHeartbeat + SESSION_TIMEOUT_MS;
        }
    }
}
