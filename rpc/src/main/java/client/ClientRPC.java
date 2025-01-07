package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.*;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

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
    private static final int                                               DEFAULT_PORT          = 12321;
    private static final String                                            DEFAULT_HOST          = "localhost";
    private static final int                                               INITIAL_RETRY_DELAY   = 1;
    private static final int                                               HEALTH_CHECK_INTERVAL = 30;
    private static       ClientRPC                                         instance;
    private final        String                                            host;
    private final        int                                               port;
    private final        Map<String, Function<JsonNode, Single<JsonNode>>> methodHandlers        = new ConcurrentHashMap<>();
    private final        AtomicLong                                        messageIdCounter      = new AtomicLong(1);
    private final        AtomicBoolean                                     running               = new AtomicBoolean(true);
    private final        ScheduledExecutorService                          healthCheckLoop       = Executors.newSingleThreadScheduledExecutor();
    private final        ScheduledExecutorService                          reConnectLoop         = Executors.newSingleThreadScheduledExecutor();
    private volatile     String                                            sessionToken          = null;
    private volatile     RPCConnection                                     connection;
    private volatile     String                                            username;
    private volatile     String                                            password;

    private ClientRPC(String host, int port, String username, String password) {
        this.host     = host;
        this.port     = port;
        this.username = username;
        this.password = password;
    }

    private ClientRPC(String username, String password) {
        this(DEFAULT_HOST, DEFAULT_PORT, username, password);
    }

    // Add singleton getInstance method
    public static synchronized ClientRPC getInstance() {
        if (instance == null) throw new IllegalStateException("ClientRPC not initialized. Call initialize() first.");
        return instance;
    }

    // Add initialization method
    public static synchronized void initialize(String username, String password) {
        if (instance == null) {
            instance = new ClientRPC(username, password);
            instance.start();
        } else instance.updateCredentials(username, password);
    }

    public synchronized void updateCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        reconnect();
    }

    /**
     * Starts the RPC client and connects to server.
     */
    public void start() {
        running.set(true);
        connect();
        startHeartbeat();
    }

    /**
     * Establishes connection to server with retry mechanism.
     */
    private void connect() {
        AtomicInteger retryDelay = new AtomicInteger(INITIAL_RETRY_DELAY);

        reConnectLoop.scheduleAtFixedRate(() -> {
            if (!running.get()) {
                reConnectLoop.shutdownNow();
                return;
            }

            try {
                Socket socket = new Socket(host, port);
                socket.setSoTimeout(30000); // 30-second timeout for socket operations

                connection = new RPCConnection("client", socket, this);

                // Start the read loop to handle incoming messages
                connection.startReadLoop();

                // Send authentication request and wait for response
                ObjectNode authParams  = JsonUtils.createObject().put("username", username).put("password", password);
                Request    authRequest = Request.create(messageIdCounter.getAndIncrement(), "auth", authParams, null);
                Response authResponse = connection.sendRequest(authRequest)
                                                  .timeout(5, TimeUnit.SECONDS) // 5-second timeout for auth
                                                  .blockingGet();

                if (authResponse.isError())
                    throw new RuntimeException("Authentication failed: " + authResponse.getErrorMessage());

                // Store session token and log success
                sessionToken = authResponse.getParams().get("sessionToken").asText();
                log.info("Connected and authenticated to server at {}:{}", host, port);

                // Shut down retry loop since the connection is established
                reConnectLoop.shutdownNow();
            } catch (Exception e) {
                if (connection != null) {
                    connection.close();
                    connection = null;
                }
                log.error("Failed to connect/authenticate to server. Retrying in {} ms", retryDelay.get(), e);
                retryDelay.set(retryDelay.get() * 2);
            }
        }, 0, retryDelay.get(), TimeUnit.MILLISECONDS);
    }

    /**
     * Starts periodic health checks.
     */
    private void startHeartbeat() {
        healthCheckLoop.scheduleAtFixedRate(() -> {
            if (sessionToken == null) {
                log.warn("Session token is null. Reconnecting...");
                reconnect();
                return;
            }

            call("health", null).subscribe( //
                    response -> log.info("Server time delta (ms): {}", System.currentTimeMillis() - JsonUtils.getLong(response.getParams(), "serverTime")), //
                    error -> {
                        log.error("Health check failed: {}", error.getMessage());
                        reconnect();
                    });
        }, HEALTH_CHECK_INTERVAL, HEALTH_CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Handles connection reconnection.
     */
    private void reconnect() {
        sessionToken = null;
        if (connection != null) connection.close();
        connect();
    }

    /**
     * Calls a remote method.
     */
    public Single<Response> call(String method, JsonNode params) {
        if (sessionToken == null) return Single.error(new IllegalStateException("Not logged in. Authenticate first."));
        if (connection == null || !connection.isActive())
            return Single.error(new IllegalStateException("Not connected to the server."));

        return connection.sendRequest(Request.create(messageIdCounter.getAndIncrement(), method, params, sessionToken));
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
        else log.info("Received unexpected success response with id: {} | {}", response.getId(), JsonUtils.toJson(response));
    }

    @Override
    public void handleDisconnect(Throwable error) {
        if (error != null) {
            log.error("Connection error", error);
            if (running.get()) reconnect();
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
        instance = null;
        log.info("RPC Client shutdown complete");
    }
}
