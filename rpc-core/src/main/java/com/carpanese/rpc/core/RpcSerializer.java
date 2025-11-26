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
        return gson.toJson(obj);
    }
    
    /**
     * Deserialize JSON string to object
     */
    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
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
     * Check if safe mode is enabled
     */
    public boolean isSafeMode() {
        return safeMode;
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
