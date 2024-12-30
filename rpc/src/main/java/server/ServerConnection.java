package server;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class ServerConnection {
    private static final Logger                             log             = LoggerFactory.getLogger(ServerConnection.class);
    private final        Socket                             socket;
    private final        DataInputStream                    in;
    private final        DataOutputStream                   out;
    private final        ObjectMapper                       objectMapper;
    private final        Object                             writeLock       = new Object();
    private final        Map<Long, SingleSubject<Response>> pendingRequests = new ConcurrentHashMap<>();

    public ServerConnection(Socket socket) {
        try {
            this.socket       = socket;
            this.in           = new DataInputStream(socket.getInputStream());
            this.out          = new DataOutputStream(socket.getOutputStream());
            this.objectMapper = new ObjectMapper();
        } catch (IOException e) {
            throw new RuntimeException("Error creating server connection", e);
        }
    }

    public String getIp() {
        return socket.getInetAddress().getHostAddress();
    }

    public Message readMessage() throws IOException {
        String  json    = in.readUTF();
        Message message = objectMapper.readValue(json, Message.class);

        if ("req".equals(message.getType())) return objectMapper.readValue(json, Request.class);
        else if ("res".equals(message.getType())) return objectMapper.readValue(json, Response.class);

        throw new IOException("Unknown message type: " + message.getType());
    }

    public Single<Response> sendRequest(Request request) {
        return sendRequestWithResponse(request);
    }

    public Single<Response> sendRequestWithResponse(Request request) {
        SingleSubject<Response> responseSubject = SingleSubject.create();
        pendingRequests.put(request.getId(), responseSubject);

        // Set up timeout
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

    public void sendResponse(Response response) {
        try {
            synchronized (writeLock) {
                out.writeUTF(objectMapper.writeValueAsString(response));
            }
        } catch (IOException e) {
            log.error("Error sending response", e);
        }
    }

    public void handleIncomingMessage(Message message) {
        if (message instanceof Response response) {
            SingleSubject<Response> pending = pendingRequests.remove(response.getId());
            if (pending != null) {
                pending.onSuccess(response);
            } else {
                log.warn("Received response for unknown request: {}", response.getId());
            }
        }
    }
}
