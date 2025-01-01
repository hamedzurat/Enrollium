package core;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import lombok.experimental.SuperBuilder;


/**
 * Represents an RPC request message.
 * Extends base Message class, adding session token for authentication.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Request extends Message {
    private String        sessionToken;  // Authentication token for the session
    @Setter
    @Getter
    private RPCConnection connection;

    /**
     * Factory method to create a new request with standard fields.
     *
     * @param id           Unique request identifier
     * @param method       RPC method name
     * @param params       Method parameters
     * @param sessionToken Authentication token
     *
     * @return New Request instance
     */
    public static Request create(long id, String method, JsonNode params, String sessionToken) {
        return Request.builder()
                      .id(id)
                      .timestamp(System.currentTimeMillis())
                      .version("1.0")
                      .type("req")
                      .method(method)
                      .params(params)
                      .sessionToken(sessionToken)
                      .build();
    }
}
