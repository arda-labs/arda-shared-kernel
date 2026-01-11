package vn.io.arda.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utility class for JSON serialization and deserialization using Jackson.
 * <p>
 * Provides convenience methods for converting objects to/from JSON strings
 * with pre-configured ObjectMapper supporting Java 8+ date/time types.
 * </p>
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Object to JSON
 * User user = new User("john", "john@example.com");
 * String json = JsonUtils.toJson(user);
 *
 * // JSON to Object
 * User parsed = JsonUtils.fromJson(json, User.class);
 *
 * // JSON to List
 * List&lt;User&gt; users = JsonUtils.fromJsonList(jsonArray, User.class);
 *
 * // JSON to Map
 * Map&lt;String, Object&gt; map = JsonUtils.toMap(json);
 * </pre>
 *
 * @author Arda Development Team
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JsonUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates and configures the default ObjectMapper.
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        return mapper;
    }

    /**
     * Converts an object to JSON string.
     *
     * @param object the object to serialize
     * @return JSON string representation
     * @throws RuntimeException if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Converts an object to pretty-printed JSON string.
     *
     * @param object the object to serialize
     * @return pretty-printed JSON string
     * @throws RuntimeException if serialization fails
     */
    public static String toPrettyJson(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to pretty JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Parses JSON string to an object of the specified type.
     *
     * @param json  the JSON string
     * @param clazz the target class type
     * @param <T>   the type parameter
     * @return the parsed object
     * @throws RuntimeException if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON to {}", clazz.getSimpleName(), e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Parses JSON string to an object using TypeReference for complex generic types.
     *
     * @param json         the JSON string
     * @param typeReference the type reference
     * @param <T>          the type parameter
     * @return the parsed object
     * @throws RuntimeException if deserialization fails
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON using TypeReference", e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Parses JSON array string to a List of objects.
     *
     * @param json  the JSON array string
     * @param clazz the element class type
     * @param <T>   the element type parameter
     * @return list of parsed objects
     * @throws RuntimeException if deserialization fails
     */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("Failed to deserialize JSON array to List<{}>", clazz.getSimpleName(), e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Converts JSON string to a Map.
     *
     * @param json the JSON string
     * @return Map representation of the JSON object
     * @throws RuntimeException if deserialization fails
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<>() {
        });
    }

    /**
     * Converts an object to a Map.
     *
     * @param object the object to convert
     * @return Map representation of the object
     */
    public static Map<String, Object> objectToMap(Object object) {
        return OBJECT_MAPPER.convertValue(object, new TypeReference<>() {
        });
    }

    /**
     * Converts a Map to an object of the specified type.
     *
     * @param map   the source map
     * @param clazz the target class type
     * @param <T>   the type parameter
     * @return the converted object
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

    /**
     * Gets the underlying ObjectMapper instance for advanced usage.
     *
     * @return the configured ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
