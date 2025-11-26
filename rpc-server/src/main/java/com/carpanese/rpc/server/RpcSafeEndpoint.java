package com.carpanese.rpc.server;

/**
 * RpcSafeEndpoint - Convenience class with Safe Mode preset enabled
 * 
 * <p>This class extends RpcEndpoint and automatically enables Safe Mode,
 * providing a cleaner API for safe RPC endpoints without manually
 * setting safeEnabled in options.</p>
 * 
 * <p>Safe Mode enables safe serialization of special types like Date,
 * NaN, Infinity, and provides better type preservation across RPC calls.</p>
 * 
 * <h3>Example Usage:</h3>
 * <pre>
 * // Instead of:
 * RpcOptions options = new RpcOptions();
 * options.setSafeEnabled(true);
 * RpcEndpoint rpc = new RpcEndpoint(context, options);
 * 
 * // Use:
 * RpcEndpoint rpc = new RpcSafeEndpoint(context);
 * </pre>
 * 
 * @see RpcEndpoint
 * @see RpcOptions
 */
public class RpcSafeEndpoint extends RpcEndpoint {
    
    /**
     * Create a new RPC endpoint with Safe Mode enabled by default
     */
    public RpcSafeEndpoint() {
        this(null, null);
    }
    
    /**
     * Create a new RPC endpoint with context and Safe Mode enabled
     * 
     * @param context Server context object passed to handlers
     */
    public RpcSafeEndpoint(Object context) {
        this(context, null);
    }
    
    /**
     * Create a new RPC endpoint with context, options, and Safe Mode enabled
     * 
     * <p>Safe Mode is automatically enabled even if not set in options.</p>
     * 
     * @param context Server context object
     * @param options Configuration options (safeEnabled is preset to true)
     */
    public RpcSafeEndpoint(Object context, RpcOptions options) {
        super(context, mergeSafeOptions(options));
    }
    
    /**
     * Merges user options with safe defaults
     * 
     * @param userOptions User-provided options or null
     * @return Options with safeEnabled set to true
     */
    private static RpcOptions mergeSafeOptions(RpcOptions userOptions) {
        RpcOptions safeOptions = userOptions != null ? userOptions : new RpcOptions();
        safeOptions.setSafeEnabled(true);
        return safeOptions;
    }
}
