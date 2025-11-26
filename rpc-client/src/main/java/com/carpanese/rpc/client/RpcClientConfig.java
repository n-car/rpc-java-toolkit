package com.carpanese.rpc.client;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for RPC Client
 */
public class RpcClientConfig {
    
    private boolean safeMode = false;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
    private Duration writeTimeout = Duration.ofSeconds(30);
    private Map<String, String> headers = new HashMap<>();
    
    public RpcClientConfig() {
        // Default headers
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
    }
    
    public boolean isSafeMode() {
        return safeMode;
    }
    
    public RpcClientConfig setSafeMode(boolean safeMode) {
        this.safeMode = safeMode;
        return this;
    }
    
    public Duration getConnectTimeout() {
        return connectTimeout;
    }
    
    public RpcClientConfig setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
    
    public Duration getReadTimeout() {
        return readTimeout;
    }
    
    public RpcClientConfig setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }
    
    public Duration getWriteTimeout() {
        return writeTimeout;
    }
    
    public RpcClientConfig setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }
    
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }
    
    public RpcClientConfig setHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    public RpcClientConfig setHeaders(Map<String, String> headers) {
        this.headers = new HashMap<>(headers);
        return this;
    }
}
