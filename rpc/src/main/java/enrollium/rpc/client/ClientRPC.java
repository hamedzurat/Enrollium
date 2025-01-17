package enrollium.rpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enrollium.design.system.memory.Volatile;
import enrollium.rpc.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
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
    private static final String                                            AUTH_SESSION_TOKEN    = "auth_session_token";
    private static final String                                            AUTH_USER_TYPE        = "auth_user_type";
    private static final String                                            AUTH_USER_ID          = "auth_user_id";
    private static final String                                            AUTH_STATUS           = "auth_status";
    private static       ClientRPC                                         instance;
    private final        String                                            host;
    private final        int                                               port;
    private final        Map<String, Function<JsonNode, Single<JsonNode>>> methodHandlers        = new ConcurrentHashMap<>();
    private final        AtomicLong                                        messageIdCounter      = new AtomicLong(1);
    private final        AtomicBoolean                                     running               = new AtomicBoolean(true);
    private final        ScheduledExecutorService                          healthCheckLoop       = Executors.newSingleThreadScheduledExecutor();
    private final        ScheduledExecutorService                          reConnectLoop         = Executors.newSingleThreadScheduledExecutor();
    private final        Subject<Boolean>                                  authStateSubject      = BehaviorSubject.create();
    private final        Object                                            connectLock           = new Object();
    private volatile     String                                            sessionToken          = null;
    private volatile     RPCConnection                                     connection;
    private volatile     String                                            email                 = null;
    private volatile     String                                            password              = null;
    private              boolean                                           connecting            = false;

    private ClientRPC(String host, int port, String email, String password) {
        this.host     = host;
        this.port     = port;
        this.email    = email;
        this.password = password;
    }

    private ClientRPC(String email, String password) {
        this(DEFAULT_HOST, DEFAULT_PORT, email, password);
    }

    // Add singleton getInstance method
    public static synchronized ClientRPC getInstance() {
        if (instance == null) throw new IllegalStateException("ClientRPC not initialized. Call initialize() first.");
        return instance;
    }

    public static Observable<ClientRPC> initialize(String username, String password) {
        return Observable.create(emitter -> {
            if (instance == null) {
                synchronized (ClientRPC.class) {
                    if (instance == null) {
                        instance = new ClientRPC(username, password);
                        instance.start();
                    }
                }
            }

            // Subscribe to auth state and pass specific error if any
            // Pass the exact error back to the UI
            instance.observeAuthState().firstElement().subscribe(authenticated -> {
                if (authenticated) {
                    emitter.onNext(instance);
                    emitter.onComplete();
                } else {
                    emitter.onError(new RuntimeException("Authentication failed"));
                }
            }, emitter::onError);
        });
    }

    public static boolean isAuthenticated() {
        return Volatile.getInstance().get(AUTH_STATUS) != null && (boolean) Volatile.getInstance().get(AUTH_STATUS);
    }

    public Observable<Boolean> observeAuthState() {
        return authStateSubject;
    }

    public synchronized void updateCredentials(String username, String password) {
        this.email    = username;
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
        synchronized (connectLock) {
            if (connecting) return;
            connecting = true;
        }

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

                if (email == null || password == null) throw new RuntimeException("Set email and password first.");

                // Send authentication request and wait for response
                ObjectNode authParams  = JsonUtils.createObject().put("email", email).put("password", password);
                Request    authRequest = Request.create(messageIdCounter.getAndIncrement(), "auth", authParams, null);
                Response authResponse = connection.sendRequest(authRequest)
                                                  .timeout(5, TimeUnit.SECONDS) // 5-second timeout for auth
                                                  .blockingGet();

                if (authResponse.isError()) {
                    // Stop retrying for invalid credentials
                    String e = authResponse.getParams().get("message").asText();
                    if (e != null || e.contains("User not found") || e.contains("Invalid password")) {
                        log.error("Authentication failed: {}", e);
                        authStateSubject.onError(new RuntimeException(e));
                        logout();
                        running.set(false);
                        return;
                    }

                    throw new RuntimeException("Authentication failed: " + authResponse.getErrorMessage());
                }

                // Store session token and log success
                JsonNode params       = authResponse.getParams();
                String   sessionToken = params.get("sessionToken").asText();
                String   userType     = params.get("userType").asText();
                String   userId       = params.get("uuid").asText();

                // Store in Volatile
                Volatile.getInstance().put(AUTH_SESSION_TOKEN, sessionToken);
                Volatile.getInstance().put(AUTH_USER_TYPE, userType);
                Volatile.getInstance().put(AUTH_USER_ID, userId);
                Volatile.getInstance().put(AUTH_STATUS, true);

                // Also store in instance for internal use
                this.sessionToken = sessionToken;

                log.info("Connected and authenticated to server at {}:{}", host, port);

                // Notify observers
                authStateSubject.onNext(true);

                // Shut down retry loop since the connection is established
                reConnectLoop.shutdownNow();
                connecting = false;
            } catch (Exception e) {
                if (connection != null) {
                    connection.close();
                    connection = null;
                }

                log.error("Failed to connect/authenticate to server. Retrying in {} ms", retryDelay.get(), e);
                retryDelay.set(retryDelay.get() * 2);

                // Notify observers of auth failure
                authStateSubject.onNext(false);
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
        if (!"auth".equals(method) && !isAuthenticated())
            return Single.error(new IllegalStateException("Not authenticated. Please login first."));
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

    public void logout() {
        running.set(false);

        // Clear auth data first
        Volatile.getInstance().remove(AUTH_SESSION_TOKEN);
        Volatile.getInstance().remove(AUTH_USER_TYPE);
        Volatile.getInstance().remove(AUTH_USER_ID);
        Volatile.getInstance().remove(AUTH_STATUS);

        // Reset connection related fields
        sessionToken = null;

        // Stop loops
        healthCheckLoop.shutdown();
        reConnectLoop.shutdown();

        // Close connection if exists
        if (connection != null) {
            connection.close();
            connection = null;
        }

        // Notify observers that auth state changed
        authStateSubject.onNext(false);

        // Reset instance
        instance = null;
        email    = null;
        password = null;

        log.info("Logged out successfully");
    }

    @Override
    public void close() {
        running.set(false);
        healthCheckLoop.shutdown();
        if (connection != null) connection.close();

        Volatile.getInstance().remove(AUTH_SESSION_TOKEN);
        Volatile.getInstance().remove(AUTH_USER_TYPE);
        Volatile.getInstance().remove(AUTH_USER_ID);
        Volatile.getInstance().remove(AUTH_STATUS);

        instance = null;
        email    = null;
        password = null;

        log.info("RPC Client shutdown complete");
    }
}
