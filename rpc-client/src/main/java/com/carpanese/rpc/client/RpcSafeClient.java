package com.carpanese.rpc.client;

/**
 * RpcSafeClient - Convenience class with Safe Mode preset enabled
 * 
 * <p>This class extends RpcClient and automatically enables Safe Mode,
 * providing a cleaner API for safe RPC clients without manually
 * setting safeEnabled in configuration.</p>
 * 
 * <p>Safe Mode enables safe deserialization of special types like Date,
 * large integers (as strings), NaN, Infinity, and provides better type
 * preservation across RPC calls.</p>
 * 
 * <h3>Example Usage:</h3>
 * <pre>
 * // Instead of:
 * RpcClientConfig config = new RpcClientConfig.Builder()
 *     .safeMode(true)
 *     .build();
 * RpcClient client = new RpcClient("http://localhost:3000/api", config);
 * 
 * // Use:
 * RpcClient client = new RpcSafeClient("http://localhost:3000/api");
 * </pre>
 * 
 * @see RpcClient
 * @see RpcClientConfig
 */
public class RpcSafeClient extends RpcClient {
    
    /**
     * Create a new RPC client with Safe Mode enabled by default
     * 
     * @param url Server URL (e.g., "http://localhost:3000/rpc")
     */
    public RpcSafeClient(String url) {
        this(url, null);
    }
    
    /**
     * Create a new RPC client with configuration and Safe Mode enabled
     * 
     * <p>Safe Mode is automatically enabled even if not set in configuration.</p>
     * 
     * @param url Server URL
     * @param config Client configuration (safeMode is preset to true), can be null
     */
    public RpcSafeClient(String url, RpcClientConfig config) {
        super(url, mergeSafeConfig(config));
    }
    
    /**
     * Merges user configuration with safe defaults
     * 
     * @param userConfig User-provided config or null
     * @return Configuration with safeMode enabled
     */
    private static RpcClientConfig mergeSafeConfig(RpcClientConfig userConfig) {
        if (userConfig == null) {
            return new RpcClientConfig().setSafeMode(true);
        }
        
        // Create new config based on user config but with safe mode enabled
        RpcClientConfig safeConfig = new RpcClientConfig()
                .setSafeMode(true)
                .setConnectTimeout(userConfig.getConnectTimeout())
                .setReadTimeout(userConfig.getReadTimeout())
                .setWriteTimeout(userConfig.getWriteTimeout())
                .setHeaders(userConfig.getHeaders());
        
        return safeConfig;
    }
}
