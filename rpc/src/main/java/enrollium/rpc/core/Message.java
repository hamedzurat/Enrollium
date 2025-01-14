package enrollium.rpc.core;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * Base message class for RPC communication.
 * Uses Lombok to reduce boilerplate code.
 * SuperBuilder allows inheritance of builder pattern.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = Request.class, name = "req"), @JsonSubTypes.Type(value = Response.class, name = "res")})
public abstract class Message {
    private long     id;         // Unique message identifier
    private long     timestamp;  // Message creation timestamp
    private String   version;    // Protocol version
    private String   type;       // Message type (req/res)
    private String   method;     // RPC method name
    private JsonNode params;     // Method parameters or response data
}
