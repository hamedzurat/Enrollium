package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class JsonUtils {
    public static String getString(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        return json.get(field).asText();
    }

    public static int getInt(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        return json.get(field).asInt();
    }

    public static ObjectNode createObject() {
        return JsonNodeFactory.instance.objectNode();
    }

    public static ObjectNode required(ObjectNode node, String... fields) {
        for (String field : fields)
            if (!node.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        return node;
    }
}
