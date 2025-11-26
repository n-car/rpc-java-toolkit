package com.carpanese.rpc.server;

import com.google.gson.JsonElement;

/**
 * Configuration for an RPC method with metadata support
 */
public class MethodConfig {
    private String name;
    private RpcMethodHandler handler;
    private JsonElement schema;
    private boolean exposeSchema;
    private String description;

    public MethodConfig() {
    }

    public MethodConfig(String name, RpcMethodHandler handler) {
        this.name = name;
        this.handler = handler;
        this.exposeSchema = false;
    }

    // Getters and Setters
    
    public String getName() {
        return name;
    }

    public MethodConfig setName(String name) {
        this.name = name;
        return this;
    }

    public RpcMethodHandler getHandler() {
        return handler;
    }

    public MethodConfig setHandler(RpcMethodHandler handler) {
        this.handler = handler;
        return this;
    }

    public JsonElement getSchema() {
        return schema;
    }

    public MethodConfig setSchema(JsonElement schema) {
        this.schema = schema;
        return this;
    }

    public boolean isExposeSchema() {
        return exposeSchema;
    }

    public MethodConfig setExposeSchema(boolean exposeSchema) {
        this.exposeSchema = exposeSchema;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MethodConfig setDescription(String description) {
        this.description = description;
        return this;
    }

    // Fluent builder methods
    
    public MethodConfig withSchema(JsonElement schema) {
        this.schema = schema;
        return this;
    }

    public MethodConfig withExposeSchema(boolean exposeSchema) {
        this.exposeSchema = exposeSchema;
        return this;
    }

    public MethodConfig withDescription(String description) {
        this.description = description;
        return this;
    }
}
