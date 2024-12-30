package core;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Message {
    private long     id;
    private long     timestamp;
    private String   version;
    private String   type;
    private String   method;
    private JsonNode params;
}
