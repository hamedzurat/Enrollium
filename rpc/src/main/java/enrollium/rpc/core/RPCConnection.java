package enrollium.rpc.core;

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
    public static final  int                                MAX_MESSAGE_SIZE        = 8 * 1024 * 1024; // 8MB
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

        socket.setSoTimeout(0);
    }

    /**
     * Reads a single message from the input stream.
     *
     * @return The parsed message, or null if connection was closed cleanly
     *
     * @throws IOException if there's an error reading the message
     */
    private Message readMessage() throws IOException {
        byte[] lengthBytes = new byte[4];

        // Read message length
        int bytesRead = in.read(lengthBytes);
        if (bytesRead == -1) return null; // Connection closed cleanly

        if (bytesRead != 4) throw new IOException("Incomplete message length read");

        // Parse and validate message length
        int messageLength = ByteBuffer.wrap(lengthBytes).getInt();
        if (messageLength <= 0 || messageLength > MAX_MESSAGE_SIZE)
            throw new IOException("Invalid message length: " + messageLength);

        // Read the message body
        byte[] messageBytes = new byte[messageLength];
        bytesRead = in.read(messageBytes);
        if (bytesRead == -1) return null; // Connection closed while reading message

        if (bytesRead != messageLength) throw new IOException("Incomplete message read");

        // Parse and return the message
        String json = new String(messageBytes, StandardCharsets.UTF_8);
        log.info("Received message: {}", json);
        return MAPPER.readValue(json, Message.class);
    }

    /**
     * Starts the message reading loop in a separate thread.
     */
    public void startReadLoop() {
        readExecutor.submit(() -> {
            while (active.get()) {
                try {
                    if (socket.isClosed()) {
                        log.warn("Socket closed, exiting read loop");
                        break;
                    }

                    Message message = readMessage();

                    if (message == null) {
                        log.info("Client disconnected: {}", getIP());
                        handleDisconnect(null);
                        break;
                    } else if (message instanceof Request request) handleIncomingRequest(request);
                    else if (message instanceof Response response) handleIncomingResponse(response);
                } catch (EOFException e) {
                    log.info("Client disconnected via exception: {}", getIP());
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

    /**
     * Waits for a specific request type with timeout.
     *
     * @param expectedMethod The method name to wait for
     *
     * @return Single that completes with the matching request
     */
    public Single<Request> waitForRequest(String expectedMethod) {
        return Single.<Request>create(emitter -> readExecutor.submit(() -> {
            try {
                while (active.get() && !socket.isClosed()) {
                    Message message = readMessage();

                    if (message == null) {
                        emitter.onError(new IOException("Connection closed while waiting for " + expectedMethod));
                        return;
                    }

                    if (message instanceof Request request && expectedMethod.equals(message.getMethod())) {
                        request.setConnection(this);
                        emitter.onSuccess(request);
                        return;
                    }
                }

                if (!active.get() || socket.isClosed())
                    emitter.onError(new IOException("Connection closed while waiting for " + expectedMethod));
            } catch (Exception e) {
                emitter.onError(e);
            }
        })).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
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
        if (pending != null) pending.onSuccess(response);
        else log.warn("Received response for unknown request: {} on connection {}", response.getId(), id);
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

            if (messageBytes.length > MAX_MESSAGE_SIZE)
                throw new IOException("Message too large: " + messageBytes.length + " bytes");

            synchronized (writeLock) {
                out.writeInt(messageBytes.length);  // Write message length first
                out.write(messageBytes);            // Then write the message
                out.flush();
            }

            log.info("Sent request: {}", json);
        } catch (IOException e) {
            pendingRequests.remove(request.getId());
            return Single.error(e);
        }

        return responseSubject.timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).doOnError(error -> {
            if (error instanceof TimeoutException) handleTimeout(request.getId());
        }).doFinally(() -> pendingRequests.remove(request.getId()));
    }

    /**
     * Handles timeout if response is not back within REQUEST_TIMEOUT_SECONDS
     */
    private void handleTimeout(long requestId) {
        SingleSubject<Response> pending = pendingRequests.remove(requestId);
        if (pending != null) pending.onSuccess(Response.error(requestId, "Request timed out"));
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
            if (error != null && !(error instanceof EOFException)) log.error("Connection error on {}", id, error);
            else {
                if (error == null) log.info("Connection {} closed by client", id);
                else log.info("Connection {} closed due to EOF", id);
            }

            messageHandler.handleDisconnect(error);
            close();
        }
    }

    /**
     * Gets the remote IP address.
     */
    public String getIP() {
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
            log.debug("Closing connection: {}", id);

            // Complete all pending requests with error
            pendingRequests.values().forEach(subject -> subject.onError(new IOException("Connection closed")));
            pendingRequests.clear();

            // Shutdown read executor
            readExecutor.shutdown();
            try {
                if (!readExecutor.awaitTermination(1, TimeUnit.SECONDS)) readExecutor.shutdownNow();
            } catch (InterruptedException e) {
                readExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Close socket
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
                log.error("Error closing socket on connection {}", id, e);
            }

            log.debug("Connection closed: {}", id);
        }
    }
}
