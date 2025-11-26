package com.carpanese.rpc.android

import com.carpanese.rpc.core.RpcRequest
import com.carpanese.rpc.core.RpcResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit interface for JSON-RPC 2.0
 * 
 * Use this interface with Retrofit for advanced HTTP features like interceptors,
 * custom converters, or integration with existing Retrofit-based architectures.
 * 
 * Example usage:
 * ```kotlin
 * val retrofit = Retrofit.Builder()
 *     .baseUrl("http://api.example.com/")
 *     .addConverterFactory(GsonConverterFactory.create())
 *     .build()
 * 
 * val rpcService = retrofit.create(RpcService::class.java)
 * 
 * val request = RpcRequest("myMethod", params, 1)
 * val response = rpcService.call("rpc", request).execute()
 * ```
 */
interface RpcService {
    
    /**
     * Make an RPC call
     * 
     * @param endpoint RPC endpoint path (e.g., "rpc", "api/v1/rpc")
     * @param request RPC request object
     * @return RPC response
     */
    @POST
    fun call(
        @Url endpoint: String,
        @Body request: RpcRequest
    ): Call<RpcResponse>
    
    /**
     * Make an RPC call (coroutine version)
     */
    @POST
    suspend fun callSuspend(
        @Url endpoint: String,
        @Body request: RpcRequest
    ): RpcResponse
}

/**
 * Retrofit-based RPC client
 * 
 * Wrapper around RpcService that provides a simpler API.
 */
class RetrofitRpcClient(
    private val service: RpcService,
    private val endpoint: String = "rpc"
) {
    
    /**
     * Call a remote method
     */
    suspend fun call(method: String, params: JsonElement? = null, id: Any = System.currentTimeMillis()): JsonElement {
        val request = RpcRequest(method, params, id)
        val response = service.callSuspend(endpoint, request)
        
        if (response.isError) {
            throw com.carpanese.rpc.core.RpcException(response.error)
        }
        
        return response.result ?: throw com.carpanese.rpc.core.RpcException(
            com.carpanese.rpc.core.RpcError.INTERNAL_ERROR,
            "Empty result"
        )
    }
    
    /**
     * Send a notification
     */
    suspend fun notify(method: String, params: JsonElement? = null) {
        val request = RpcRequest.notification(method, params)
        service.callSuspend(endpoint, request)
    }
}
