package enrollium.rpc.server;

import com.fasterxml.jackson.databind.JsonNode;
import enrollium.rpc.core.MessageHandler;
import enrollium.rpc.core.RPCConnection;
import enrollium.rpc.core.Request;
import enrollium.rpc.core.Response;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;


/**
 * Server-side RPC implementation that handles incoming connections and message routing.
 */
@Slf4j
public class ServerRPC implements AutoCloseable, MessageHandler {
    private static final    int                                                          DEFAULT_PORT       = 12321;
    private static volatile ServerRPC                                                    instance;
    private final           Map<String, BiFunction<JsonNode, Request, Single<JsonNode>>> methodHandlers     = new ConcurrentHashMap<>();
    private final           ExecutorService                                              connectionExecutor = Executors.newCachedThreadPool();
    private final           AtomicLong                                                   messageIdCounter   = new AtomicLong(1);
    @Getter
    private final           SessionManager                                               sessionManager;
    private final           RateLimiter                                                  rateLimiter;
    private final           int                                                          port;
    private volatile        boolean                                                      running            = true;
    private                 ServerSocket                                                 serverSocket;

    private ServerRPC(int port) {
        this.port = port;
        RateLimiter.getInstance();
        SessionManager.initialize(this);
        this.sessionManager = SessionManager.getInstance();
        this.rateLimiter    = RateLimiter.getInstance();
    }

    private ServerRPC() {
        this(DEFAULT_PORT);
    }

    public static synchronized ServerRPC getInstance() {
        if (instance == null) instance = new ServerRPC();
        return instance;
    }

    public static synchronized void initialize() {
        initialize(DEFAULT_PORT);
    }

    public static synchronized void initialize(int port) {
        if (instance != null) throw new IllegalStateException("ServerRPC already initialized");
        instance = new ServerRPC(port);
    }

    /**
     * Starts the RPC server.
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        log.info("RPC Server started on port {}", port);

        // Accept connections in a separate thread
        connectionExecutor.submit(this::acceptConnections);
    }

    /**
     * Accepts incoming connections.
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleNewConnection(clientSocket);
            } catch (IOException e) {
                if (running) log.error("Error accepting connection", e);
            }
        }
    }

    /**
     * Handles a new client connection.
     */
    private void handleNewConnection(Socket socket) {
        String ip = socket.getInetAddress().getHostAddress();

        if (rateLimiter.isRequestDenied(ip)) {
            log.warn("Connection rejected due to rate limiting: {}", ip);
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error closing rate-limited socket", e);
            }
            return;
        }

        try {
            // Create the RPC connection
            RPCConnection connection = new RPCConnection("tmp", socket, this);

            // Wait for the auth request with timeout
            connection.waitForRequest("auth").timeout(30, TimeUnit.SECONDS).subscribe(authRequest -> {
                // Process auth request using the registered auth method handler
                handleRequest(authRequest).subscribe(authResponse -> {
                    if (authResponse.isError()) {
                        connection.sendResponse(authResponse);
                        connection.close();
                    } else {
                        // Send success response
                        connection.sendResponse(authResponse);
                        // Start the read loop for subsequent messages
                        connection.startReadLoop();
                    }
                }, error -> {
                    log.error("Error during authentication", error);
                    connection.sendResponse(Response.error(authRequest.getId(), "Authentication failed: " + error.getMessage()));
                    connection.close();
                });
            }, error -> {
                log.error("Auth failed for connection from {}: {}", ip, error.getMessage());
                connection.close();
            });
        } catch (Exception e) {
            log.error("Error handling new connection from {}", ip, e);
            try {
                socket.close();
            } catch (IOException ex) {
                log.error("Error closing socket after connection failure", ex);
            }
        }
    }

    @Override
    public Single<Response> handleRequest(Request request) {
        // Handle rate limiting
        String rateLimitKey = request.getSessionToken() != null
                              ? request.getSessionToken()
                              : request.getConnection().getIP();
        if (rateLimiter.isRequestDenied(rateLimitKey))
            return Single.just(Response.error(request.getId(), "Rate limited"));

        // For non-auth requests, validate session token
        if (!"auth".equals(request.getMethod()) && (request.getSessionToken() == null || sessionManager.validateSession(request.getSessionToken())))
            return Single.just(Response.error(request.getId(), "Invalid session"));

        // Update session heartbeat for authenticated requests
        if (request.getSessionToken() != null) sessionManager.updateHeartbeat(request.getSessionToken());

        // Get method handler
        BiFunction<JsonNode, Request, Single<JsonNode>> handler = methodHandlers.get(request.getMethod());
        if (handler == null)
            return Single.just(Response.error(request.getId(), "Unknown method: " + request.getMethod()));

        // Execute handler
        return handler.apply(request.getParams(), request)
                      .map(result -> Response.success(request.getId(), result))
                      .onErrorReturn(error -> Response.error(request.getId(), error.getMessage()));
    }

    @Override
    public void handleResponse(Response response) {
        if (response.isError()) log.warn("Received error response: {}", response.getErrorMessage());
        else log.debug("Received success response for request: {}", response.getId());
    }

    @Override
    public void handleDisconnect(Throwable error) {
        if (error != null) log.error("Connection error", error);
    }

    /**
     * Registers a method handler.
     */
    public void registerMethod(String method, BiFunction<JsonNode, Request, Single<JsonNode>> handler) {
        methodHandlers.put(method, handler);
    }

    /**
     * Sends a request to a specific session.
     */
    public Single<Response> call(String method, JsonNode params, String sessionToken) {
        if (sessionManager.validateSession(sessionToken))
            return Single.error(new IllegalStateException("Invalid session"));

        Request request = Request.create(messageIdCounter.getAndIncrement(), method, params, sessionToken);
        return sessionManager.sendRequest(sessionToken, request);
    }

    /**
     * Broadcasts a request to sessions with specified tags.
     */
    public void broadcast(String method, JsonNode params, List<String> tags) {
        Request request = Request.create(messageIdCounter.getAndIncrement(), method, params, null);
        sessionManager.broadcast(request, tags);
    }

    @Override
    public void close() {
        running  = false;
        instance = null;

        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            log.error("Error closing server socket", e);
        }

        connectionExecutor.shutdown();
        sessionManager.close();
        rateLimiter.close();

        log.info("RPC Server shutdown complete");
    }
}
