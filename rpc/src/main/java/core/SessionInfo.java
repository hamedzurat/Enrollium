package core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.reactivex.rxjava3.core.Single;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Represents an active session in the RPC system.
 * Manages session state, authentication, and connection.
 */
@Data
@Slf4j
public class SessionInfo {
    private static final long          SESSION_TIMEOUT_MS = TimeUnit.DAYS.toMillis(1);
    private final        String        sessionToken;          // Unique session identifier
    private final        String        userId;                // Associated user identifier
    private final        Set<String>   tags;                  // Session tags for grouping/filtering
    @JsonIgnore
    private final        RPCConnection connection;            // Network connection
    private final        long          createdAt;             // Session creation timestamp
    private volatile     long          lastHeartbeat;         // Last activity timestamp
    private volatile     long          expirationTime;        // Session expiration timestamp

    /**
     * Creates a new session with specified parameters.
     */
    public SessionInfo(String sessionToken, String userId, RPCConnection connection) {
        this.sessionToken   = sessionToken;
        this.userId         = userId;
        this.connection     = connection;
        this.tags           = ConcurrentHashMap.newKeySet();
        this.createdAt      = System.currentTimeMillis();
        this.lastHeartbeat  = this.createdAt;
        this.expirationTime = this.createdAt + SESSION_TIMEOUT_MS;
    }

    /**
     * Updates session heartbeat and expiration time.
     */
    public void updateHeartbeat() {
        this.lastHeartbeat  = System.currentTimeMillis();
        this.expirationTime = this.lastHeartbeat + SESSION_TIMEOUT_MS;
    }

    /**
     * Checks if session has expired.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Checks if session has missed too many heartbeats.
     */
    public boolean hasHeartbeatTimeout(long timeoutMs) {
        return (System.currentTimeMillis() - lastHeartbeat) > timeoutMs;
    }

    /**
     * Sends a request through the session's connection.
     */
    public Single<Response> sendRequest(Request request) {
        if (!connection.isActive()) return Single.error(new IllegalStateException("Session connection is not active"));

        return connection.sendRequest(request);
    }

    /**
     * Sends a response through the session's connection.
     */
    public void sendResponse(Response response) {
        if (connection.isActive()) connection.sendResponse(response);
        else log.warn("Attempted to send response on inactive session: {}", sessionToken);
    }

    /**
     * Gets the remote IP address of the session.
     */
    public String getRemoteAddress() {
        return connection.getRemoteAddress();
    }

    /**
     * Checks if the session is currently active.
     */
    public boolean isActive() {
        return connection.isActive() && !isExpired();
    }

    /**
     * Closes the session and its connection.
     */
    public void close() {
        connection.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInfo that = (SessionInfo) o;
        return sessionToken.equals(that.sessionToken);
    }

    @Override
    public int hashCode() {
        return sessionToken.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Session[token=%s, user=%s, remote=%s]", sessionToken, userId, getRemoteAddress());
    }
}
