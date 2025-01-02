package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.*;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;


/**
 * Server-side RPC implementation that handles incoming connections and message routing.
 */
@Slf4j
public class ServerRPC implements AutoCloseable, MessageHandler {
    private static final int                                                         DEFAULT_PORT       = 12321;
    private final        Map<String, BiFunction<JsonNode, String, Single<JsonNode>>> methodHandlers     = new ConcurrentHashMap<>();
    private final        ExecutorService                                             connectionExecutor = Executors.newCachedThreadPool();
    private final        AtomicLong                                                  messageIdCounter   = new AtomicLong(1);
    private final        SessionManager                                              sessionManager;
    private final        RateLimiter                                                 rateLimiter;
    private final        int                                                         port;
    private volatile     boolean                                                     running            = true;
    private              ServerSocket                                                serverSocket;

    public ServerRPC(int port) {
        this.port           = port;
        this.rateLimiter    = new RateLimiter();
        this.sessionManager = new SessionManager(this);
    }

    public ServerRPC() {
        this(DEFAULT_PORT);
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

        if (!rateLimiter.allowRequest(ip)) {
            log.warn("Connection rejected due to rate limiting: {}", ip);
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error closing rate-limited socket", e);
            }
            return;
        }

        try {
            // Create connection
            RPCConnection connection = new RPCConnection("tmp", socket, this);

            // Handle first auth message
            Request firstRequest = connection.waitForRequest("auth").blockingGet();

            String username = JsonUtils.getString(firstRequest.getParams(), "username");
            String password = JsonUtils.getString(firstRequest.getParams(), "password");

            if (username == null || password == null) {
                connection.sendResponse(Response.error(firstRequest.getId(), "Invalid credentials"));
                connection.close();
                return;
            }

            // Create authenticated session
            String      userId  = "user-" + System.nanoTime();
            SessionInfo session = sessionManager.createSession(userId, socket, this);

            // Send success response with session token
            ObjectNode response = JsonUtils.createObject().put("sessionToken", session.getSessionToken());
            connection.sendResponse(Response.success(firstRequest.getId(), response));

            log.info("New authenticated connection from: {} with session: {}", ip, session.getSessionToken());
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
        // Handle auth requests without session validation
        if ("auth".equals(request.getMethod())) return handleAuth(request);

        String token = request.getSessionToken();

        // Validate session for all other requests
        if (token == null || sessionManager.validateSession(token))
            return Single.just(Response.error(request.getId(), "Invalid session"));

        // Update session heartbeat
        sessionManager.updateHeartbeat(token);

        // Handle special cases first
        if ("health".equals(request.getMethod())) return handleHealthCheck(request);

        if (!rateLimiter.allowRequest(token)) {
            log.warn("Request rejected due to rate limiting: {}", token);
            return Single.just(Response.error(request.getId(), "Rate limited session"));
        }

        // Get method handler
        BiFunction<JsonNode, String, Single<JsonNode>> handler = methodHandlers.get(request.getMethod());
        if (handler == null)
            return Single.just(Response.error(request.getId(), "Unknown method: " + request.getMethod()));

        // Execute handler
        return handler.apply(request.getParams(), request.getSessionToken())
                      .map(result -> Response.success(request.getId(), result))
                      .onErrorReturn(error -> Response.error(request.getId(), error.getMessage()));
    }

    private Single<Response> handleAuth(Request request) {
        try {
            // Validate request parameters
            if (request.getParams() == null) {
                return Single.just(Response.error(request.getId(), "Missing auth parameters"));
            }

            String username = JsonUtils.getString(request.getParams(), "username");
            String password = JsonUtils.getString(request.getParams(), "password");

            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return Single.just(Response.error(request.getId(), "Invalid credentials"));
            }

            // For demo, accept any credentials
            String userId = "user-" + System.nanoTime();

            // Create new session
            Socket socket = request.getConnection()
                                   .getSocket();  // You'll need to add getConnection() to Request class
            SessionInfo session = sessionManager.createSession(userId, socket, this);

            ObjectNode response = JsonUtils.createObject().put("sessionToken", session.getSessionToken());
            return Single.just(Response.success(request.getId(), response));
        } catch (Exception e) {
            log.error("Auth error", e);
            return Single.just(Response.error(request.getId(), "Authentication failed: " + e.getMessage()));
        }
    }

    /**
     * Handles health check requests.
     */
    private Single<Response> handleHealthCheck(Request request) {
        ObjectNode params = JsonUtils.createObject()
                                     .put("serverTime", System.currentTimeMillis())
                                     .put("serverVersion", "1.0.0");
        return Single.just(Response.success(request.getId(), params));
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
    public void registerMethod(String method, BiFunction<JsonNode, String, Single<JsonNode>> handler) {
        methodHandlers.put(method, handler);
    }

    /**
     * Sends a request to a specific session.
     */
    public Single<Response> call(String method, JsonNode params, String sessionToken) {
        if (sessionManager.validateSession(sessionToken)) {
            return Single.error(new IllegalStateException("Invalid session"));
        }

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
        running = false;

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
