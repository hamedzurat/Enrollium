package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * Represents an RPC response message.
 * Extends base Message class, providing factory methods for success and error responses.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Response extends Message {
    /**
     * Creates a successful response with given data.
     *
     * @param id     Original request ID
     * @param params Response data
     *
     * @return Success Response instance
     */
    public static Response success(long id, JsonNode params) {
        return Response.builder()
                       .id(id)
                       .timestamp(System.currentTimeMillis())
                       .version("1.0")
                       .type("res")
                       .method("success")
                       .params(params)
                       .build();
    }

    /**
     * Creates an error response with an error message.
     *
     * @param id           Original request ID
     * @param errorMessage Error description
     *
     * @return Error Response instance
     */
    public static Response error(long id, String errorMessage) {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("message", errorMessage);

        return Response.builder()
                       .id(id)
                       .timestamp(System.currentTimeMillis())
                       .version("1.0")
                       .type("res")
                       .method("error")
                       .params(params)
                       .build();
    }

    /**
     * Helper method to check if this response represents an error.
     *
     * @return true if this is an error response
     */
    public boolean isError() {
        return "error".equals(getMethod());
    }

    /**
     * Helper method to get error message from error response.
     *
     * @return error message or null if this is not an error response
     */
    public String getErrorMessage() {
        if (!isError() || getParams() == null) {
            return null;
        }
        return getParams().get("message").asText();
    }
}
