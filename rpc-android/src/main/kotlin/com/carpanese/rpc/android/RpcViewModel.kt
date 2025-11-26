package com.carpanese.rpc.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carpanese.rpc.core.RpcException
import com.google.gson.JsonElement
import kotlinx.coroutines.launch

/**
 * ViewModel for RPC calls with LiveData support
 * 
 * Provides reactive state management for RPC operations in Android apps.
 * 
 * Example usage:
 * ```kotlin
 * class MyViewModel : RpcViewModel("http://api.example.com/rpc") {
 *     
 *     fun loadData() {
 *         callRpc("getData") { result ->
 *             // Handle result
 *         }
 *     }
 * }
 * 
 * // In Activity/Fragment
 * viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
 *     // Show/hide progress
 * }
 * 
 * viewModel.error.observe(viewLifecycleOwner) { error ->
 *     // Show error message
 * }
 * ```
 */
open class RpcViewModel(
    private val url: String,
    private val config: com.carpanese.rpc.client.RpcClientConfig = com.carpanese.rpc.client.RpcClientConfig()
) : ViewModel() {
    
    @PublishedApi
    internal val client = RpcClientKt(url, config)
    
    @PublishedApi
    internal val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    
    @PublishedApi
    internal val _error = MutableLiveData<RpcException?>(null)
    val error: LiveData<RpcException?> = _error
    
    /**
     * Call an RPC method with automatic loading and error handling
     * 
     * @param method Method name
     * @param params Method parameters
     * @param onSuccess Callback for successful result
     */
    protected fun callRpc(
        method: String,
        params: JsonElement? = null,
        onSuccess: (JsonElement) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                val result = client.call(method, params)
                onSuccess(result)
            } catch (e: RpcException) {
                _error.value = e
            } catch (e: Exception) {
                _error.value = RpcException(
                    com.carpanese.rpc.core.RpcError.INTERNAL_ERROR,
                    "Network error: ${e.message}",
                    e
                )
            } finally {
                _loading.value = false
            }
        }
    }
    
    /**
     * Call an RPC method with type-safe result
     */
    protected inline fun <reified T> callRpcAs(
        method: String,
        params: JsonElement? = null,
        crossinline onSuccess: (T) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                val result = client.callAs<T>(method, params)
                onSuccess(result)
            } catch (e: RpcException) {
                _error.value = e
            } catch (e: Exception) {
                _error.value = RpcException(
                    com.carpanese.rpc.core.RpcError.INTERNAL_ERROR,
                    "Network error: ${e.message}",
                    e
                )
            } finally {
                _loading.value = false
            }
        }
    }
    
    /**
     * Send a notification
     */
    protected fun notifyRpc(method: String, params: JsonElement? = null) {
        viewModelScope.launch {
            try {
                client.notify(method, params)
            } catch (e: Exception) {
                // Notifications are fire-and-forget, log error but don't update UI
                android.util.Log.w("RpcViewModel", "Notification failed: ${e.message}")
            }
        }
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
    
    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
