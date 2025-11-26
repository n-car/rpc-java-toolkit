package com.carpanese.rpc.android

import com.carpanese.rpc.client.RpcClient
import com.carpanese.rpc.client.RpcClientConfig
import com.carpanese.rpc.core.RpcException
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Kotlin Coroutines wrapper for RpcClient
 * 
 * Provides suspend functions for making RPC calls in Android apps.
 * 
 * Example usage:
 * ```kotlin
 * val client = RpcClientKt("http://api.example.com/rpc")
 * 
 * lifecycleScope.launch {
 *     try {
 *         val result = client.call("myMethod", params)
 *         // Handle result
 *     } catch (e: RpcException) {
 *         // Handle error
 *     }
 * }
 * ```
 */
class RpcClientKt(
    url: String,
    config: RpcClientConfig = RpcClientConfig()
) : AutoCloseable {
    
    private val client = RpcClient(url, config)
    
    /**
     * Call a remote method (suspend function)
     * 
     * @param method Method name
     * @param params Method parameters (can be null)
     * @return Result as JsonElement
     * @throws RpcException If the call fails or returns an error
     */
    suspend fun call(method: String, params: JsonElement? = null): JsonElement = withContext(Dispatchers.IO) {
        client.call(method, params)
    }
    
    /**
     * Call a remote method with custom request ID
     */
    suspend fun call(method: String, params: JsonElement?, id: Any): JsonElement = withContext(Dispatchers.IO) {
        client.call(method, params, id)
    }
    
    /**
     * Send a notification (no response expected)
     */
    suspend fun notify(method: String, params: JsonElement? = null) = withContext(Dispatchers.IO) {
        client.notify(method, params)
    }
    
    /**
     * Set authentication token
     */
    fun setAuthToken(token: String) {
        client.setAuthToken(token)
    }
    
    /**
     * Clear authentication token
     */
    fun clearAuth() {
        client.clearAuth()
    }
    
    /**
     * Get the server URL
     */
    val url: String
        get() = client.url
    
    /**
     * Check if safe mode is enabled
     */
    val safeMode: Boolean
        get() = client.isSafeMode
    
    override fun close() {
        client.close()
    }
}

/**
 * Extension function to call RPC methods with type-safe result
 * 
 * Example:
 * ```kotlin
 * data class User(val name: String, val email: String)
 * val user: User = client.callAs("getUser", params)
 * ```
 */
suspend inline fun <reified T> RpcClientKt.callAs(
    method: String,
    params: JsonElement? = null
): T {
    val result = call(method, params)
    return com.google.gson.Gson().fromJson(result, T::class.java)
}
