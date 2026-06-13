package com.carpanese.rpc.core;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * JSON-RPC 2.0 Serializer with Safe Mode support
 *
 * Safe Mode serialization:
 * - Strings: "hello" -> "S:hello"
 * - Dates: ISO 8601 -> "D:2025-11-26T10:30:00Z"
 * - BigInteger: 123456789 -> "123456789n"
 */
public class RpcSerializer {

    private final Gson gson;
    private final boolean safeMode;

    /**
     * Creates a new serializer
     *
     * @param safeMode Enable type-safe serialization with prefixes
     */
    public RpcSerializer(boolean safeMode) {
        this.safeMode = safeMode;

        GsonBuilder builder = new GsonBuilder();

        if (safeMode) {
            // Register custom serializers for Safe Mode
            builder.registerTypeAdapter(String.class, new SafeStringSerializer());
            builder.registerTypeAdapter(Instant.class, new SafeDateSerializer());
            builder.registerTypeAdapter(BigInteger.class, new SafeBigIntegerSerializer());
        }

        this.gson = builder
                .serializeNulls()
                .create();
    }

    /**
     * Creates a serializer with safe mode disabled
     */
    public RpcSerializer() {
        this(false);
    }

    /**
     * Serialize object to JSON string
     */
    public String toJson(Object obj) {
        if (obj instanceof RpcRequest request) {
            return gson.toJson(toSafeRequestJson(request));
        }

        if (safeMode) {
            if (obj instanceof RpcResponse response) {
                return gson.toJson(toSafeResponseJson(response));
            }
            if (obj instanceof RpcError error) {
                return gson.toJson(toSafeErrorJson(error));
            }
        }

        return gson.toJson(obj);
    }

    /**
     * Deserialize JSON string to object
     */
    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        if (safeMode) {
            JsonElement element = JsonParser.parseString(json);

            if (classOfT == RpcRequest.class) {
                return classOfT.cast(gson.fromJson(fromSafeRequestJson(element), classOfT));
            }
            if (classOfT == RpcResponse.class) {
                return classOfT.cast(gson.fromJson(fromSafeResponseJson(element), classOfT));
            }
            if (classOfT == RpcError.class) {
                return classOfT.cast(gson.fromJson(fromSafeErrorJson(element), classOfT));
            }
            if (classOfT == JsonElement.class) {
                return classOfT.cast(decodeSafe(element));
            }
        }

        return gson.fromJson(json, classOfT);
    }

    /**
     * Deserialize JSON string to object with type
     */
    public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        return gson.fromJson(json, typeOfT);
    }

    /**
     * Parse JSON string to JsonElement
     */
    public JsonElement parse(String json) throws JsonSyntaxException {
        return JsonParser.parseString(json);
    }

    /**
     * Encode a JsonElement recursively using Safe Mode markers.
     */
    public JsonElement encodeSafe(JsonElement value) {
        if (!safeMode || value == null || value.isJsonNull()) {
            return value == null ? JsonNull.INSTANCE : value.deepCopy();
        }

        if (value.isJsonArray()) {
            JsonArray result = new JsonArray();
            for (JsonElement item : value.getAsJsonArray()) {
                result.add(encodeSafe(item));
            }
            return result;
        }

        if (value.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (var entry : value.getAsJsonObject().entrySet()) {
                result.add(entry.getKey(), encodeSafe(entry.getValue()));
            }
            return result;
        }

        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isString()) {
            return new JsonPrimitive("S:" + primitive.getAsString());
        }
        if (primitive.isBoolean()) {
            return new JsonPrimitive(primitive.getAsBoolean());
        }
        if (primitive.isNumber()) {
            return new JsonPrimitive(primitive.getAsNumber());
        }

        return value.deepCopy();
    }

    /**
     * Decode a JsonElement recursively from Safe Mode markers.
     */
    public JsonElement decodeSafe(JsonElement value) {
        if (!safeMode || value == null || value.isJsonNull()) {
            return value == null ? JsonNull.INSTANCE : value.deepCopy();
        }

        if (value.isJsonArray()) {
            JsonArray result = new JsonArray();
            for (JsonElement item : value.getAsJsonArray()) {
                result.add(decodeSafe(item));
            }
            return result;
        }

        if (value.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (var entry : value.getAsJsonObject().entrySet()) {
                result.add(entry.getKey(), decodeSafe(entry.getValue()));
            }
            return result;
        }

        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (!primitive.isString()) {
            return value.deepCopy();
        }

        String stringValue = primitive.getAsString();
        if (stringValue.startsWith("S:")) {
            return new JsonPrimitive(stringValue.substring(2));
        }
        if (stringValue.startsWith("D:")) {
            return new JsonPrimitive(stringValue.substring(2));
        }

        return new JsonPrimitive(stringValue);
    }

    /**
     * Check if safe mode is enabled
     */
    public boolean isSafeMode() {
        return safeMode;
    }

    private JsonObject toSafeRequestJson(RpcRequest request) {
        JsonObject result = new JsonObject();
        result.addProperty("jsonrpc", request.getJsonrpc());
        result.addProperty("method", request.getMethod());
        if (request.getParams() != null && !request.getParams().isJsonNull()) {
            result.add("params", encodeSafe(request.getParams()));
        }
        if (request.getId() != null) {
            result.add("id", gson.toJsonTree(request.getId()));
        }
        return result;
    }

    private JsonElement fromSafeRequestJson(JsonElement element) {
        JsonObject result = element.getAsJsonObject().deepCopy();
        if (result.has("params")) {
            result.add("params", decodeSafe(result.get("params")));
        }
        return result;
    }

    private JsonObject toSafeResponseJson(RpcResponse response) {
        JsonObject result = new JsonObject();
        result.addProperty("jsonrpc", response.getJsonrpc());
        if (response.isError()) {
            result.add("error", toSafeErrorJson(response.getError()));
        } else {
            result.add("result", encodeSafe(response.getResult()));
        }
        result.add("id", response.getId() == null ? JsonNull.INSTANCE : gson.toJsonTree(response.getId()));
        return result;
    }

    private JsonElement fromSafeResponseJson(JsonElement element) {
        JsonObject result = element.getAsJsonObject().deepCopy();
        if (result.has("result")) {
            result.add("result", decodeSafe(result.get("result")));
        }
        if (result.has("error") && result.get("error").isJsonObject()) {
            result.add("error", fromSafeErrorJson(result.get("error")));
        }
        return result;
    }

    private JsonObject toSafeErrorJson(RpcError error) {
        JsonObject result = new JsonObject();
        result.addProperty("code", error.getCode());
        result.add("message", encodeSafe(new JsonPrimitive(error.getMessage())));
        if (error.getData() != null && !error.getData().isJsonNull()) {
            result.add("data", encodeSafe(error.getData()));
        }
        return result;
    }

    private JsonElement fromSafeErrorJson(JsonElement element) {
        JsonObject result = element.getAsJsonObject().deepCopy();
        if (result.has("message")) {
            result.add("message", decodeSafe(result.get("message")));
        }
        if (result.has("data")) {
            result.add("data", decodeSafe(result.get("data")));
        }
        return result;
    }

    // Safe Mode Serializers

    private static class SafeStringSerializer implements JsonSerializer<String>, JsonDeserializer<String> {
        @Override
        public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive("S:" + src);
        }

        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String value = json.getAsString();
            if (value.startsWith("S:")) {
                return value.substring(2);
            }
            return value;
        }
    }

    private static class SafeDateSerializer implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            String iso = DateTimeFormatter.ISO_INSTANT.format(src);
            return new JsonPrimitive("D:" + iso);
        }

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String value = json.getAsString();
            if (value.startsWith("D:")) {
                return Instant.parse(value.substring(2));
            }
            return Instant.parse(value);
        }
    }

    private static class SafeBigIntegerSerializer implements JsonSerializer<BigInteger>, JsonDeserializer<BigInteger> {
        @Override
        public JsonElement serialize(BigInteger src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString() + "n");
        }

        @Override
        public BigInteger deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String value = json.getAsString();
            if (value.endsWith("n")) {
                return new BigInteger(value.substring(0, value.length() - 1));
            }
            return new BigInteger(value);
        }
    }
}
