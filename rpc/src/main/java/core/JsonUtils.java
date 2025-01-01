package core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Utility class for JSON operations with strong validation and error handling.
 */
@Slf4j
public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Creates an empty ObjectNode.
     */
    public static ObjectNode createObject() {
        return JsonNodeFactory.instance.objectNode();
    }

    /**
     * Creates an empty ArrayNode.
     */
    public static ArrayNode createArray() {
        return JsonNodeFactory.instance.arrayNode();
    }

    /**
     * Gets a required string field from JsonNode.
     *
     * @throws IllegalArgumentException if field is missing or not a string
     */
    public static String getString(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        JsonNode node = json.get(field);
        if (!node.isTextual()) throw new IllegalArgumentException("Field is not a string: " + field);

        return node.asText();
    }

    /**
     * Gets an optional string field from JsonNode.
     */
    public static Optional<String> getStringOptional(JsonNode json, String field) {
        return Optional.ofNullable(json.get(field)).filter(JsonNode::isTextual).map(JsonNode::asText);
    }

    /**
     * Gets a required integer field from JsonNode.
     *
     * @throws IllegalArgumentException if field is missing or not an integer
     */
    public static int getInt(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        JsonNode node = json.get(field);
        if (!node.isInt()) throw new IllegalArgumentException("Field is not an integer: " + field);

        return node.asInt();
    }

    /**
     * Gets an optional integer field from JsonNode.
     */
    public static Optional<Integer> getIntOptional(JsonNode json, String field) {
        return Optional.ofNullable(json.get(field)).filter(JsonNode::isInt).map(JsonNode::asInt);
    }

    /**
     * Gets a required long field from JsonNode.
     *
     * @throws IllegalArgumentException if field is missing or not a long
     */
    public static long getLong(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        JsonNode node = json.get(field);
        if (!node.isLong() && !node.isInt()) throw new IllegalArgumentException("Field is not a long: " + field);

        return node.asLong();
    }

    /**
     * Gets a required double field from JsonNode.
     *
     * @throws IllegalArgumentException if field is missing or not a number
     */
    public static double getDouble(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        JsonNode node = json.get(field);
        if (!node.isNumber()) throw new IllegalArgumentException("Field is not a number: " + field);

        return node.asDouble();
    }

    /**
     * Gets a required boolean field from JsonNode.
     *
     * @throws IllegalArgumentException if field is missing or not a boolean
     */
    public static boolean getBoolean(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        JsonNode node = json.get(field);
        if (!node.isBoolean()) throw new IllegalArgumentException("Field is not a boolean: " + field);

        return node.asBoolean();
    }

    /**
     * Gets a required array field from JsonNode.
     *
     * @throws IllegalArgumentException if field is missing or not an array
     */
    public static ArrayNode getArray(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        JsonNode node = json.get(field);
        if (!node.isArray()) throw new IllegalArgumentException("Field is not an array: " + field);

        return (ArrayNode) node;
    }

    /**
     * Gets a required object field from JsonNode.
     *
     * @throws IllegalArgumentException if field is missing or not an object
     */
    public static ObjectNode getObject(JsonNode json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("Required field missing: " + field);

        JsonNode node = json.get(field);
        if (!node.isObject()) throw new IllegalArgumentException("Field is not an object: " + field);

        return (ObjectNode) node;
    }

    /**
     * Validates that an ObjectNode has all required fields.
     *
     * @throws IllegalArgumentException if any required field is missing
     */
    public static ObjectNode required(ObjectNode node, String... fields) {
        List<String> missing = new ArrayList<>();
        for (String field : fields)
            if (!node.has(field)) missing.add(field);

        if (!missing.isEmpty())
            throw new IllegalArgumentException("Required fields missing: " + String.join(", ", missing));

        return node;
    }

    /**
     * Converts an object to JsonNode.
     *
     * @throws RuntimeException if conversion fails
     */
    public static JsonNode toJson(Object obj) {
        try {
            return MAPPER.valueToTree(obj);
        } catch (Exception e) {
            log.error("Failed to convert object to JSON: {}", obj, e);
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Converts a JsonNode to a specific class type.
     *
     * @throws RuntimeException if conversion fails
     */
    public static <T> T fromJson(JsonNode node, Class<T> clazz) {
        try {
            return MAPPER.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to {}: {}", clazz.getSimpleName(), node, e);
            throw new RuntimeException("Failed to convert JSON to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Deep copy of a JsonNode.
     */
    public static JsonNode deepCopy(JsonNode node) {
        return node.deepCopy();
    }
}
