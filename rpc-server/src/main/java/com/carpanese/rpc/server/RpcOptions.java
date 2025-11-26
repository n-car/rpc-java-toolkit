package com.carpanese.rpc.server;

import com.carpanese.rpc.server.logging.RpcLoggerOptions;

/**
 * Configuration options for RPC Endpoint
 */
public class RpcOptions {
    private boolean safeEnabled = false;
    private boolean warnOnUnsafe = true;
    private boolean enableBatch = true;
    private int maxBatchSize = 100;
    private boolean enableLogging = true;
    private RpcLoggerOptions loggerOptions = new RpcLoggerOptions();
    private boolean enableMiddleware = true;
    private boolean enableValidation = true;
    private boolean sanitizeErrors = true;
    private int timeoutSeconds = 30;
    private boolean enableIntrospection = false;
    private String introspectionPrefix = "__rpc";

    public RpcOptions() {
    }

    // Getters and Setters

    public boolean isSafeEnabled() {
        return safeEnabled;
    }

    public RpcOptions setSafeEnabled(boolean safeEnabled) {
        this.safeEnabled = safeEnabled;
        return this;
    }

    public boolean isWarnOnUnsafe() {
        return warnOnUnsafe;
    }

    public RpcOptions setWarnOnUnsafe(boolean warnOnUnsafe) {
        this.warnOnUnsafe = warnOnUnsafe;
        return this;
    }

    public boolean isEnableBatch() {
        return enableBatch;
    }

    public RpcOptions setEnableBatch(boolean enableBatch) {
        this.enableBatch = enableBatch;
        return this;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public RpcOptions setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
        return this;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public RpcOptions setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return this;
    }

    public RpcLoggerOptions getLoggerOptions() {
        return loggerOptions;
    }

    public RpcOptions setLoggerOptions(RpcLoggerOptions loggerOptions) {
        this.loggerOptions = loggerOptions;
        return this;
    }

    public boolean isEnableMiddleware() {
        return enableMiddleware;
    }

    public RpcOptions setEnableMiddleware(boolean enableMiddleware) {
        this.enableMiddleware = enableMiddleware;
        return this;
    }

    public boolean isEnableValidation() {
        return enableValidation;
    }

    public RpcOptions setEnableValidation(boolean enableValidation) {
        this.enableValidation = enableValidation;
        return this;
    }

    public boolean isSanitizeErrors() {
        return sanitizeErrors;
    }

    public RpcOptions setSanitizeErrors(boolean sanitizeErrors) {
        this.sanitizeErrors = sanitizeErrors;
        return this;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public RpcOptions setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public boolean isEnableIntrospection() {
        return enableIntrospection;
    }

    public RpcOptions setEnableIntrospection(boolean enableIntrospection) {
        this.enableIntrospection = enableIntrospection;
        return this;
    }

    public String getIntrospectionPrefix() {
        return introspectionPrefix;
    }

    public RpcOptions setIntrospectionPrefix(String introspectionPrefix) {
        this.introspectionPrefix = introspectionPrefix;
        return this;
    }
}
