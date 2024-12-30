package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.JsonUtils;
import core.Message;
import core.Request;
import core.Response;
import io.reactivex.rxjava3.core.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;


public class ServerRPC implements AutoCloseable {
    private static final    Logger                                                      log              = LoggerFactory.getLogger(ServerRPC.class);
    private static final    int                                                         SERVER_PORT      = 12321;
    private static final    Object                                                      mutex            = new Object();
    // Singleton instance
    private static volatile ServerRPC                                                   instance;
    private final           SessionManager                                              sessionManager;
    private final           RateLimiter                                                 rateLimiter;
    private final           Map<String, BiFunction<JsonNode, String, Single<JsonNode>>> methodHandlers   = new ConcurrentHashMap<>();
    private final           ExecutorService                                             executorService;
    private final           AtomicLong                                                  messageIdCounter = new AtomicLong(1);
    private volatile        boolean                                                     running          = true;
    private                 ServerSocket                                                serverSocket;

    private ServerRPC() {
        this.sessionManager  = new SessionManager();
        this.rateLimiter     = new RateLimiter();
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public static ServerRPC getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    instance = new ServerRPC();
                }
            }
        }
        return instance;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
        log.info("Server started on port {}", SERVER_PORT);

        new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewConnection(clientSocket);
                } catch (IOException e) {
                    if (running) {
                        log.error("Error accepting client connection", e);
                    }
                }
            }
        }, "ServerRPC-Acceptor").start();
    }

    private void handleNewConnection(Socket clientSocket) {
        ServerConnection connection = new ServerConnection(clientSocket);
        executorService.submit(() -> {
            try {
                while (running && !clientSocket.isClosed()) {
                    Message message = connection.readMessage();

                    switch (message) {
                        case Request request -> handleRequest(request, connection);
                        case Response response -> connection.handleIncomingMessage(message);
                        case null, default -> {}
                    }
                }
            } catch (IOException e) {
                log.error("Error handling client connection", e);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    log.error("Error closing client socket", e);
                }
            }
        });
    }

    private void handleRequest(Request request, ServerConnection connection) {
        String ip = connection.getIp();

        if (!rateLimiter.allowRequest(ip)) {
            connection.sendResponse(Response.error(request.getId(), "Rate limit exceeded"));
            return;
        }

        // Handle health check
        if ("health".equals(request.getMethod())) {
            ObjectNode healthParams = JsonUtils.createObject();
            healthParams.put("serverTime", System.currentTimeMillis());
            healthParams.put("serverVersion", "1.0.0");
            connection.sendResponse(Response.success(request.getId(), healthParams));
            return;
        }

        // Handle login
        if ("login".equals(request.getMethod())) {
            handleLogin(request, connection);
            return;
        }

        // Validate session for all other requests
        if (!sessionManager.validateSession(request.getSessionToken())) {
            connection.sendResponse(Response.error(request.getId(), "Invalid session. Login please."));
            return;
        }

        // Update heartbeat
        sessionManager.updateHeartbeat(request.getSessionToken());

        // Handle method call
        BiFunction<JsonNode, String, Single<JsonNode>> handler = methodHandlers.get(request.getMethod());
        if (handler == null) {
            connection.sendResponse(Response.error(request.getId(), "Unknown method"));
            return;
        }

        handler.apply(request.getParams(), request.getSessionToken())
               .subscribe(result -> connection.sendResponse(Response.success(request.getId(), result)), error -> connection.sendResponse(Response.error(request.getId(), error.getMessage())));
    }

    private void handleLogin(Request request, ServerConnection connection) {
        try {
            String username = JsonUtils.getString(request.getParams(), "username");
            String password = JsonUtils.getString(request.getParams(), "password");

            // In a real implementation, validate credentials here
            String userId = "user-" + System.currentTimeMillis();
            String token  = sessionManager.createSession(userId, connection);

            ObjectNode response = JsonUtils.createObject();
            response.put("session_token", token);
            connection.sendResponse(Response.success(request.getId(), response));
        } catch (Exception e) {
            connection.sendResponse(Response.error(request.getId(), e.getMessage()));
        }
    }

    public void registerMethod(String method, BiFunction<JsonNode, String, Single<JsonNode>> handler) {
        methodHandlers.put(method, handler);
    }

    public Single<Response> call(String method, JsonNode params, String sessionToken) {
        ServerConnection connection = sessionManager.getConnection(sessionToken);
        if (connection == null) {
            return Single.error(new IllegalStateException("Invalid session"));
        }

        Request request = Request.create(messageIdCounter.getAndIncrement(), method, params, sessionToken);
        return connection.sendRequestWithResponse(request);
    }

    public void broadcast(String method, JsonNode params, List<String> tags) {
        Set<String> sessions = sessionManager.getSessionsByTags(tags);
        Request     request  = Request.create(messageIdCounter.getAndIncrement(), method, params, null);

        for (String sessionToken : sessions) {
            ServerConnection connection = sessionManager.getConnection(sessionToken);
            if (connection != null) {
                try {
                    connection.sendRequest(request).subscribe();
                } catch (Exception e) {
                    log.error("Error broadcasting to session: {}", sessionToken, e);
                }
            }
        }
    }

    @Override
    public void close() {
        running = false;
        executorService.shutdown();
        sessionManager.shutdown();
        rateLimiter.shutdown();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing server socket", e);
        }
    }
}
