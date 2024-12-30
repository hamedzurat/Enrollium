package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.*;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;


/**
 * Client-side RPC implementation that manages server connection and message handling.
 */
@Slf4j
public class ClientRPC implements AutoCloseable, MessageHandler {
    private static final int                                               DEFAULT_PORT             = 12321;
    private static final String                                            DEFAULT_HOST             = "localhost";
    private static final int                                               INITIAL_RETRY_DELAY_MS   = 1000;
    private static final int                                               MAX_RETRY_DELAY_MS       = 30000;
    private static final int                                               HEALTH_CHECK_INTERVAL_MS = 30000;
    private final        String                                            host;
    private final        int                                               port;
    private final        Map<String, Function<JsonNode, Single<JsonNode>>> methodHandlers           = new ConcurrentHashMap<>();
    private final        AtomicLong                                        messageIdCounter         = new AtomicLong(1);
    private final        AtomicBoolean                                     running                  = new AtomicBoolean(true);
    private final        ScheduledExecutorService                          healthCheckLoop          = Executors.newSingleThreadScheduledExecutor();
    private final        ScheduledExecutorService                          reConnectLoop            = Executors.newSingleThreadScheduledExecutor();
    private volatile     String                                            sessionToken;
    private volatile     RPCConnection                                     connection;
    // Store last login credentials for session restoration
    private volatile     String                                            lastUsername;
    private volatile     String                                            lastPassword;

    public ClientRPC(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ClientRPC() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Starts the RPC client and connects to server.
     */
    public void start() {
        running.set(true);
        connect();
        startHealthCheck();
    }

    /**
     * Establishes connection to server with retry mechanism.
     */
    private void connect() {
        AtomicInteger retryDelay = new AtomicInteger(INITIAL_RETRY_DELAY_MS);

        reConnectLoop.scheduleAtFixedRate(() -> {
            // Check if we should keep trying to connect
            if (!running.get()) {
                // If not running, forcefully shutdown and interrupt the scheduler
                reConnectLoop.shutdownNow();
                return;
            }

            try {
                Socket socket = new Socket(host, port);
                connection = new RPCConnection("client", socket, this);
                log.info("Connected to server at {}:{}", host, port);
                // On successful connection, shutdown the scheduler
                reConnectLoop.shutdownNow();
            } catch (IOException e) {
                log.error("Failed to connect to server. Retrying in {} ms", retryDelay.get(), e);
                // Double the delay time up to max allowed delay
                retryDelay.set(Math.min(retryDelay.get() * 2, MAX_RETRY_DELAY_MS));
            }
        }, 0, retryDelay.get(), TimeUnit.MILLISECONDS);
    }

    /**
     * Starts periodic health checks.
     */
    private void startHealthCheck() {
        healthCheckLoop.scheduleAtFixedRate(() -> {
            if (connection != null && connection.isActive()) {
                healthCheck().subscribe(//
                        response -> log.debug("Health check successful"), //
                        error -> {
                            log.error("Health check failed", error);
                            reconnect();
                        });
            }
        }, HEALTH_CHECK_INTERVAL_MS, HEALTH_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Performs connection health check.
     */
    private Single<Response> healthCheck() {
        Request request = Request.create(messageIdCounter.getAndIncrement(), "health", null, sessionToken);
        return connection.sendRequest(request);
    }

    /**
     * Handles connection reconnection.
     */
    private void reconnect() {
        if (connection != null) connection.close();

        connect();
        // Attempt to restore session
        if (sessionToken != null) login(lastUsername, lastPassword).subscribe(//
                newToken -> log.info("Session restored after reconnection"),//
                error -> log.error("Failed to restore session after reconnection", error)//
        );
    }

    /**
     * Performs login and session establishment.
     */
    public Single<String> login(String username, String password) {
        this.lastUsername = username;
        this.lastPassword = password;

        ObjectNode params  = JsonUtils.createObject().put("username", username).put("password", password);
        Request    request = Request.create(messageIdCounter.getAndIncrement(), "login", params, null);

        return connection.sendRequest(request).map(response -> {
            if (response.isError()) throw new RuntimeException("Login failed: " + response.getErrorMessage());
            sessionToken = JsonUtils.getString(response.getParams(), "sessionToken");
            return sessionToken;
        });
    }

    /**
     * Calls a remote method.
     */
    public Single<Response> call(String method, JsonNode params) {
        if (sessionToken == null) return Single.error(new IllegalStateException("Not logged in"));
        if (!connection.isActive()) return Single.error(new IllegalStateException("Not connected to server"));

        Request request = Request.create(messageIdCounter.getAndIncrement(), method, params, sessionToken);
        return connection.sendRequest(request);
    }

    @Override
    public Single<Response> handleRequest(Request request) {
        Function<JsonNode, Single<JsonNode>> handler = methodHandlers.get(request.getMethod());
        if (handler == null)
            return Single.just(Response.error(request.getId(), "Unknown method: " + request.getMethod()));

        return handler.apply(request.getParams())
                      .map(result -> Response.success(request.getId(), result))
                      .onErrorReturn(error -> Response.error(request.getId(), error.getMessage()));
    }

    @Override
    public void handleResponse(Response response) {
        // Responses are primarily handled by the subscribers to the call() method
        // This method handles any server-initiated responses that aren't part of a client call

        if (response.isError()) log.warn("Received unexpected error response: {}", response.getErrorMessage());
        else log.debug("Received unexpected success response for request: {}", response.getId());
    }

    @Override
    public void handleDisconnect(Throwable error) {
        if (error != null) {
            log.error("Connection error", error);
            if (running.get()) {
                reconnect();
            }
        }
    }

    /**
     * Registers a method handler for server-initiated requests.
     */
    public void registerMethod(String method, Function<JsonNode, Single<JsonNode>> handler) {
        methodHandlers.put(method, handler);
    }

    @Override
    public void close() {
        running.set(false);
        healthCheckLoop.shutdown();
        if (connection != null) connection.close();
        log.info("RPC Client shutdown complete");
    }
}
