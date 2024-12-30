package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.JsonUtils;
import core.Message;
import core.Request;
import core.Response;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.SingleSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;


public class ClientRPC implements AutoCloseable {
    private static final    Logger                                            log                      = LoggerFactory.getLogger(ClientRPC.class);
    private static final    int                                               SERVER_PORT              = 12321;
    private static final    String                                            SERVER_HOST              = "localhost";
    private static final    int                                               INITIAL_RETRY_DELAY_MS   = 10000; // 10 seconds
    private static final    int                                               HEALTH_CHECK_INTERVAL_MS = 30;
    private static final    Object                                            mutex                    = new Object();
    // Singleton instance
    private static volatile ClientRPC                                         instance;
    private final           ObjectMapper                                      objectMapper             = new ObjectMapper();
    private final           Map<String, Function<JsonNode, Single<JsonNode>>> methodHandlers           = new ConcurrentHashMap<>();
    private final           Map<Long, SingleSubject<Response>>                pendingRequests          = new ConcurrentHashMap<>();
    private final           AtomicLong                                        messageIdCounter         = new AtomicLong(1);
    private final           ScheduledExecutorService                          scheduler                = Executors.newSingleThreadScheduledExecutor();
    private final           ExecutorService                                   executor                 = Executors.newSingleThreadExecutor();
    private final           Object                                            writeLock                = new Object();
    private volatile        boolean                                           running                  = true;
    private volatile        String                                            sessionToken             = null;
    private                 Socket                                            socket;
    private                 DataInputStream                                   in;
    private                 DataOutputStream                                  out;

    private ClientRPC() {}

    public static ClientRPC getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    instance = new ClientRPC();
                }
            }
        }
        return instance;
    }

    public void start() {
        running = true;
        connect();
        startHealthCheck();
    }

    private void startHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                healthCheck().subscribe(response -> log.debug("Health check successful"), error -> log.error("Health check failed", error));
            } catch (Exception e) {
                log.error("Error during health check", e);
            }
        }, HEALTH_CHECK_INTERVAL_MS, HEALTH_CHECK_INTERVAL_MS, TimeUnit.SECONDS);
    }

    private void connect() {
        int retryDelay = INITIAL_RETRY_DELAY_MS;

        while (running) {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                in     = new DataInputStream(socket.getInputStream());
                out    = new DataOutputStream(socket.getOutputStream());

                executor.submit(this::messageReadingLoop);
                log.info("Connected to server successfully");

                return;
            } catch (IOException e) {
                log.error("Failed to connect to server. Retrying in {} ms", retryDelay, e);
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                retryDelay = Math.min(retryDelay * 2, 120000);
            }
        }
    }

    private void messageReadingLoop() {
        while (running && !socket.isClosed()) {
            try {
                String  json    = in.readUTF();
                Message message = objectMapper.readValue(json, Message.class);

                if ("req".equals(message.getType())) handleRequest(objectMapper.convertValue(message, Request.class));
                else if ("res".equals(message.getType())) handleResponse(objectMapper.convertValue(message, Response.class));
            } catch (IOException e) {
                if (running) {
                    log.error("Error reading message", e);
                    reconnect();
                }
                break;
            }
        }
    }

    private void handleRequest(Request request) {
        Function<JsonNode, Single<JsonNode>> handler = methodHandlers.get(request.getMethod());
        if (handler == null) {
            sendResponse(Response.error(request.getId(), "Unknown method"));
            return;
        }

        handler.apply(request.getParams())
               .subscribe(result -> sendResponse(Response.success(request.getId(), result)), error -> sendResponse(Response.error(request.getId(), error.getMessage())));
    }

    private void handleResponse(Response response) {
        SingleSubject<Response> pending = pendingRequests.remove(response.getId());

        if (pending != null) pending.onSuccess(response);
        else log.warn("Received response for unknown request: {}", response.getId());
    }

    private void sendResponse(Response response) {
        try {
            synchronized (writeLock) {
                out.writeUTF(objectMapper.writeValueAsString(response));
            }
        } catch (IOException e) {
            log.error("Error sending response", e);
            reconnect();
        }
    }

    public Single<Response> healthCheck() {
        Request request = Request.create(messageIdCounter.getAndIncrement(), "health", null, null);
        return sendRequest(request);
    }

    public Single<String> login(String username, String password) {
        ObjectNode params = JsonUtils.createObject().put("username", username).put("password", password);

        Request request = Request.create(messageIdCounter.getAndIncrement(), "login", params, null);

        return sendRequest(request).map(response -> {
            if ("success".equals(response.getMethod())) {
                sessionToken = JsonUtils.getString(response.getParams(), "session_token");
                return sessionToken;
            }
            throw new RuntimeException("Login failed: " + response.getParams());
        });
    }

    public Single<Response> call(String method, JsonNode params) {
        if (sessionToken == null) {
            return Single.error(new IllegalStateException("Not logged in"));
        }

        Request request = Request.create(messageIdCounter.getAndIncrement(), method, params, sessionToken);
        return sendRequest(request);
    }

    private Single<Response> sendRequest(Request request) {
        SingleSubject<Response> responseSubject = SingleSubject.create();
        pendingRequests.put(request.getId(), responseSubject);

        responseSubject.timeout(30, TimeUnit.SECONDS)
                       .doFinally(() -> pendingRequests.remove(request.getId()))
                       .subscribe(response -> {}, error -> log.error("Request {} failed", request.getId(), error));

        try {
            synchronized (writeLock) {
                out.writeUTF(objectMapper.writeValueAsString(request));
            }
        } catch (IOException e) {
            pendingRequests.remove(request.getId());
            return Single.error(e);
        }

        return responseSubject;
    }

    public void registerMethod(String method, Function<JsonNode, Single<JsonNode>> handler) {
        methodHandlers.put(method, handler);
    }

    private void reconnect() {
        closeConnection();
        connect();
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            log.error("Error closing socket", e);
        }
    }

    @Override
    public void close() {
        running = false;
        closeConnection();
        scheduler.shutdown();
        executor.shutdown();
    }
}
