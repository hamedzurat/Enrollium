package core;

import io.reactivex.rxjava3.core.Single;


/**
 * Interface for handling incoming messages in RPC communication.
 * Provides callback methods for requests and responses.
 */
public interface MessageHandler {
    /**
     * Handles incoming request messages.
     *
     * @param request The incoming request
     *
     * @return Single that completes when request is handled
     */
    Single<Response> handleRequest(Request request);
    /**
     * Handles incoming response messages.
     *
     * @param response The incoming response
     */
    void handleResponse(Response response);
    /**
     * Called when connection is closed or encounters error.
     *
     * @param error The error that caused disconnection, or null if clean shutdown
     */
    void handleDisconnect(Throwable error);
}
