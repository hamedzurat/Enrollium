package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class Response extends Message {
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
}
