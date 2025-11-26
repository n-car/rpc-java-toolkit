package com.carpanese.rpc.android

import com.carpanese.rpc.core.RpcException
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Flow-based RPC operations for reactive programming
 * 
 * Example usage:
 * ```kotlin
 * rpcFlow("http://api.example.com/rpc") {
 *     call("myMethod", params)
 * }.catch { error ->
 *     // Handle error
 * }.collect { result ->
 *     // Handle result
 * }
 * ```
 */

/**
 * Create a Flow that emits the result of an RPC call
 */
fun rpcFlow(
    url: String,
    config: com.carpanese.rpc.client.RpcClientConfig = com.carpanese.rpc.client.RpcClientConfig(),
    block: suspend RpcClientKt.() -> JsonElement
): Flow<JsonElement> = flow {
    val client = RpcClientKt(url, config)
    try {
        val result = client.block()
        emit(result)
    } finally {
        client.close()
    }
}

/**
 * Create a Flow with type-safe result
 */
inline fun <reified T> rpcFlowAs(
    url: String,
    config: com.carpanese.rpc.client.RpcClientConfig = com.carpanese.rpc.client.RpcClientConfig(),
    crossinline block: suspend RpcClientKt.() -> JsonElement
): Flow<T> = flow {
    val client = RpcClientKt(url, config)
    try {
        val result = client.block()
        val typed = com.google.gson.Gson().fromJson(result, T::class.java)
        emit(typed)
    } finally {
        client.close()
    }
}

/**
 * Sealed class for RPC call results (Success/Error)
 */
sealed class RpcResult<out T> {
    data class Success<T>(val data: T) : RpcResult<T>()
    data class Error(val exception: RpcException) : RpcResult<Nothing>()
    object Loading : RpcResult<Nothing>()
}

/**
 * Create a Flow that emits RpcResult states (Loading -> Success/Error)
 */
fun rpcResultFlow(
    url: String,
    config: com.carpanese.rpc.client.RpcClientConfig = com.carpanese.rpc.client.RpcClientConfig(),
    block: suspend RpcClientKt.() -> JsonElement
): Flow<RpcResult<JsonElement>> = flow {
    emit(RpcResult.Loading)
    
    val client = RpcClientKt(url, config)
    try {
        val result = client.block()
        emit(RpcResult.Success(result))
    } catch (e: RpcException) {
        emit(RpcResult.Error(e))
    } catch (e: Exception) {
        emit(RpcResult.Error(
            RpcException(
                com.carpanese.rpc.core.RpcError.INTERNAL_ERROR,
                "Network error: ${e.message}",
                e
            )
        ))
    } finally {
        client.close()
    }
}

/**
 * Type-safe version of rpcResultFlow
 */
inline fun <reified T> rpcResultFlowAs(
    url: String,
    config: com.carpanese.rpc.client.RpcClientConfig = com.carpanese.rpc.client.RpcClientConfig(),
    crossinline block: suspend RpcClientKt.() -> JsonElement
): Flow<RpcResult<T>> = flow {
    emit(RpcResult.Loading)
    
    val client = RpcClientKt(url, config)
    try {
        val result = client.block()
        val typed = com.google.gson.Gson().fromJson(result, T::class.java)
        emit(RpcResult.Success(typed))
    } catch (e: RpcException) {
        emit(RpcResult.Error(e))
    } catch (e: Exception) {
        emit(RpcResult.Error(
            RpcException(
                com.carpanese.rpc.core.RpcError.INTERNAL_ERROR,
                "Network error: ${e.message}",
                e
            )
        ))
    } finally {
        client.close()
    }
}
