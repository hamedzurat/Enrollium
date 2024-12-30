package core;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class Request extends Message {
    private String sessionToken;

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
