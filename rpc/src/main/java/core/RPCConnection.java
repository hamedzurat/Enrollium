package core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.SingleSubject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Handles network communication for RPC, managing socket connections and message passing.
 */
@Slf4j
public class RPCConnection implements AutoCloseable {
    private static final int                                REQUEST_TIMEOUT_SECONDS = 30;
    private static final ObjectMapper                       MAPPER                  = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
    private              BufferedOutputStream               bufferedOut;
    private              BufferedInputStream                bufferedIn;

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
        this.in             = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out            = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        startReadLoop();
    }

    /**
     * Starts the message reading loop in a separate thread.
     */
    private void startReadLoop() {
        readExecutor.submit(() -> {
            byte[] lengthBytes = new byte[4];
            while (active.get()) {
                try {
                    if (socket.isClosed()) {
                        log.warn("Socket closed, exiting read loop");
                        break;
                    }

                    // First read message length
                    int bytesRead = in.read(lengthBytes);
                    if (bytesRead == -1) { // Client closed the connection
                        log.info("Client disconnected: {}", getRemoteAddress());
                        handleDisconnect(null); // Trigger disconnect handling
                        break;
                    }
                    if (bytesRead != 4) { // Invalid message length read
                        throw new IOException("Incomplete message length read");
                    }

                    int messageLength = ByteBuffer.wrap(lengthBytes).getInt();
                    if (messageLength <= 0 || messageLength > 1048576) { // Max 1MB
                        throw new IOException("Invalid message length: " + messageLength);
                    }

                    // Then read the actual message
                    byte[] messageBytes = new byte[messageLength];
                    bytesRead = in.read(messageBytes);
                    if (bytesRead == -1) { // Client closed the connection
                        log.info("Client disconnected while reading message: {}", getRemoteAddress());
                        handleDisconnect(null);
                        break;
                    }
                    if (bytesRead != messageLength) { // Incomplete message
                        throw new IOException("Incomplete message read");
                    }

                    String json = new String(messageBytes, StandardCharsets.UTF_8);
                    log.info("Received message: {}", json);

                    Message message = MAPPER.readValue(json, Message.class);

                    if (message instanceof Request) {
                        handleIncomingRequest((Request) message);
                    } else if (message instanceof Response) {
                        handleIncomingResponse((Response) message);
                    }
                } catch (EOFException e) {
                    log.info("Client disconnected: {}", getRemoteAddress());
                    handleDisconnect(null);
                    break;
                } catch (IOException e) {
                    if (active.get()) {
                        log.error("Error reading message", e);
                        handleDisconnect(e);
                    }
                    break;
                } catch (Exception e) {
                    log.error("Unexpected error in read loop", e);
                    handleDisconnect(e);
                    break;
                }
            }
        });
    }
    //    private void startReadLoop() {
    //        readExecutor.submit(() -> {
    //            byte[] lengthBytes = new byte[4];
    //            while (active.get()) {
    //                try {
    //                    if (socket.isClosed()) {
    //                        log.warn("Socket closed, exiting read loop");
    //                        break;
    //                    }
    //
    //                    // First read message length
    //                    int bytesRead = in.read(lengthBytes);
    //                    if (bytesRead != 4) {
    //                        if (bytesRead == -1) {
    //                            log.info("End of stream reached");
    //                            break;
    //                        }
    //                        throw new IOException("Incomplete message length read");
    //                    }
    //
    //                    int messageLength = ByteBuffer.wrap(lengthBytes).getInt();
    //                    if (messageLength <= 0 || messageLength > 1048576) { // Max 1MB
    //                        throw new IOException("Invalid message length: " + messageLength);
    //                    }
    //
    //                    // Then read the actual message
    //                    byte[] messageBytes = new byte[messageLength];
    //                    bytesRead = in.read(messageBytes);
    //                    if (bytesRead != messageLength) {
    //                        throw new IOException("Incomplete message read");
    //                    }
    //
    //                    String json = new String(messageBytes, StandardCharsets.UTF_8);
    //                    log.info("Received message: {}", json);
    //
    //                    Message message = MAPPER.readValue(json, Message.class);
    //
    //                    if (message instanceof Request) {
    //                        handleIncomingRequest((Request) message);
    //                    } else if (message instanceof Response) {
    //                        handleIncomingResponse((Response) message);
    //                    }
    //                } catch (EOFException e) {
    //                    if (active.get()) {
    //                        log.info("Connection closed by peer");
    //                        handleDisconnect(null);
    //                    }
    //                    break;
    //                } catch (IOException e) {
    //                    if (active.get()) {
    //                        log.error("Error reading message", e);
    //                        handleDisconnect(e);
    //                    }
    //                    break;
    //                } catch (Exception e) {
    //                    log.error("Unexpected error in read loop", e);
    //                    handleDisconnect(e);
    //                    break;
    //                }
    //            }
    //        });
    //    }

    /**
     *
     */
    private void handleTimeout(long requestId) {
        SingleSubject<Response> pending = pendingRequests.remove(requestId);
        if (pending != null) {
            Response timeoutResponse = Response.error(requestId, "Request timed out");
            pending.onSuccess(timeoutResponse);

            // If there are no more pending requests, check connection health
            if (pendingRequests.isEmpty()) {
                checkConnectionHealth();
            }
        }
    }

    private void checkConnectionHealth() {
        if (!isActive()) {
            return;
        }

        try {
            String json = MAPPER.writeValueAsString(Request.create(0, "health", null, null));
            synchronized (writeLock) {
                out.writeUTF(json);
                out.flush();
            }
        } catch (IOException e) {
            log.error("Health check failed", e);
            handleDisconnect(e);
        }
    }

    /**
     * Handles incoming request messages.
     */
    private void handleIncomingRequest(Request request) {
        request.setConnection(this);
        messageHandler.handleRequest(request)
                      .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                      .subscribe(this::sendResponse, error -> {
                          log.error("Error handling request on connection {}", id, error);
                          String message = error instanceof TimeoutException
                                           ? "Request processing timed out"
                                           : error.getMessage();
                          sendResponse(Response.error(request.getId(), message));
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

        try {
            String json         = MAPPER.writeValueAsString(request);
            byte[] messageBytes = json.getBytes(StandardCharsets.UTF_8);

            synchronized (writeLock) {
                // Write message length first
                out.writeInt(messageBytes.length);
                // Then write the message
                out.write(messageBytes);
                out.flush();
            }
            log.info("Sent request: {}", json);
        } catch (IOException e) {
            pendingRequests.remove(request.getId());
            return Single.error(e);
        }

        return responseSubject.timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).doOnError(error -> {
            if (error instanceof TimeoutException) {
                handleTimeout(request.getId());
            }
        }).doFinally(() -> pendingRequests.remove(request.getId()));
    }

    /**
     * Sends a response to a request.
     *
     * @param response The response to send
     */
    public void sendResponse(Response response) {
        try {
            String json         = MAPPER.writeValueAsString(response);
            byte[] messageBytes = json.getBytes(StandardCharsets.UTF_8);

            synchronized (writeLock) {
                out.writeInt(messageBytes.length);
                out.write(messageBytes);
                out.flush();
            }
            log.info("Sent response: {}", json);
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
            if (error != null && !(error instanceof EOFException)) {
                log.error("Connection error on {}", id, error);
            } else if (error == null) {
                log.info("Connection {} closed by client", id);
            } else {
                log.info("Connection {} closed due to EOF", id);
            }

            messageHandler.handleDisconnect(error);
            close();
        }
    }
    //    private void handleDisconnect(Throwable error) {
    //        if (active.compareAndSet(true, false)) {
    //            if (error != null && !(error instanceof EOFException)) {
    //                log.error("Connection error on {}", id, error);
    //            } else {
    //                log.info("Connection {} closed", id);
    //            }
    //
    //            messageHandler.handleDisconnect(error);
    //            close();
    //        }
    //    }

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

    /**
     *
     */
    public Single<Request> waitForRequest(String expectedMethod) {
        return Single.create(emitter -> {
            // Implementation to wait for specific request
            // This is a simplified version - you might want to add timeout and better error handling
            readExecutor.submit(() -> {
                try {
                    while (active.get()) {
                        String  json    = in.readUTF();
                        Message message = MAPPER.readValue(json, Message.class);
                        if (message instanceof Request && expectedMethod.equals(message.getMethod())) {
                            emitter.onSuccess((Request) message);
                            break;
                        }
                    }
                } catch (Exception e) {
                    emitter.onError(e);
                }
            });
        });
    }

    @Override
    public void close() {
        if (active.compareAndSet(true, false)) {
            log.debug("Closing connection: {}", id);

            // Complete all pending requests with error
            pendingRequests.values().forEach(subject -> subject.onError(new IOException("Connection closed")));
            pendingRequests.clear();

            // Shutdown read executor
            readExecutor.shutdown();
            try {
                if (!readExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    readExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                readExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Close socket
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                log.error("Error closing socket on connection {}", id, e);
            }

            log.debug("Connection closed: {}", id);
        }
    }
}
