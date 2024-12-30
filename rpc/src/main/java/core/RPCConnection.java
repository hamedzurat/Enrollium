package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.SingleSubject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Handles network communication for RPC, managing socket connections and message passing.
 */
@Slf4j
public class RPCConnection implements AutoCloseable {
    private static final int                                REQUEST_TIMEOUT_SECONDS = 30;
    private static final ObjectMapper                       MAPPER                  = new ObjectMapper();
    @Getter
    private final        String                             id;
    @Getter
    private final        Socket                             socket;
    private final        DataInputStream                    in;
    private final        DataOutputStream                   out;
    private final        MessageHandler                     messageHandler;
    private final        Map<Long, SingleSubject<Response>> pendingRequests         = new ConcurrentHashMap<>();
    private final        ExecutorService                    readExecutor            = Executors.newSingleThreadExecutor();
    private final        Object                             writeLock               = new Object();
    private final        AtomicBoolean                      active                  = new AtomicBoolean(true);

    /**
     * Creates a new RPC connection.
     *
     * @param id             Connection identifier
     * @param socket         Connected socket
     * @param messageHandler Handler for incoming messages
     */
    public RPCConnection(String id, Socket socket, MessageHandler messageHandler) throws IOException {
        this.id             = id;
        this.socket         = socket;
        this.messageHandler = messageHandler;
        this.in             = new DataInputStream(socket.getInputStream());
        this.out            = new DataOutputStream(socket.getOutputStream());

        startReadLoop();
    }

    /**
     * Starts the message reading loop in a separate thread.
     */
    private void startReadLoop() {
        readExecutor.submit(() -> {
            while (active.get() && !socket.isClosed()) {
                try {
                    String  json    = in.readUTF();
                    Message message = MAPPER.readValue(json, Message.class);

                    // Handle different message types
                    if ("req".equals(message.getType())) {
                        handleIncomingRequest(MAPPER.convertValue(message, Request.class));
                    } else if ("res".equals(message.getType())) {
                        handleIncomingResponse(MAPPER.convertValue(message, Response.class));
                    } else {
                        log.warn("Unknown message type received: {}", message.getType());
                    }
                } catch (IOException e) {
                    if (active.get()) {
                        log.error("Error reading message on connection {}", id, e);
                        handleDisconnect(e);
                    }
                    break;
                }
            }
        });
    }

    /**
     * Handles incoming request messages.
     */
    private void handleIncomingRequest(Request request) {
        messageHandler.handleRequest(request).subscribe(response -> sendResponse(response), error -> {
            log.error("Error handling request on connection {}", id, error);
            sendResponse(Response.error(request.getId(), error.getMessage()));
        });
    }

    /**
     * Handles incoming response messages.
     */
    private void handleIncomingResponse(Response response) {
        SingleSubject<Response> pending = pendingRequests.remove(response.getId());
        if (pending != null) {
            pending.onSuccess(response);
            messageHandler.handleResponse(response);
        } else {
            log.warn("Received response for unknown request: {} on connection {}", response.getId(), id);
        }
    }

    /**
     * Sends a request and waits for response.
     *
     * @param request The request to send
     *
     * @return Single that completes with the response
     */
    public Single<Response> sendRequest(Request request) {
        SingleSubject<Response> responseSubject = SingleSubject.create();
        pendingRequests.put(request.getId(), responseSubject);

        // Set up timeout
        responseSubject.timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                       .doFinally(() -> pendingRequests.remove(request.getId()))
                       .subscribe(response -> {}, error -> log.error("Request {} failed on connection {}", request.getId(), id, error));

        // Send the request
        try {
            synchronized (writeLock) {
                out.writeUTF(MAPPER.writeValueAsString(request));
            }
        } catch (IOException e) {
            pendingRequests.remove(request.getId());
            return Single.error(e);
        }

        return responseSubject;
    }

    /**
     * Sends a response to a request.
     *
     * @param response The response to send
     */
    public void sendResponse(Response response) {
        try {
            synchronized (writeLock) {
                out.writeUTF(MAPPER.writeValueAsString(response));
            }
        } catch (IOException e) {
            log.error("Error sending response on connection {}", id, e);
            handleDisconnect(e);
        }
    }

    /**
     * Handles disconnection events.
     */
    private void handleDisconnect(Throwable error) {
        if (active.compareAndSet(true, false)) {
            messageHandler.handleDisconnect(error);

            // Complete all pending requests with error
            pendingRequests.values().forEach(subject -> subject.onError(new IOException("Connection closed", error)));
            pendingRequests.clear();

            close();
        }
    }

    /**
     * Gets the remote IP address.
     */
    public String getRemoteAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    /**
     * Checks if the connection is active.
     */
    public boolean isActive() {
        return active.get() && !socket.isClosed();
    }

    @Override
    public void close() {
        if (active.compareAndSet(true, false)) {
            readExecutor.shutdown();
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
                log.error("Error closing socket on connection {}", id, e);
            }
        }
    }
}
