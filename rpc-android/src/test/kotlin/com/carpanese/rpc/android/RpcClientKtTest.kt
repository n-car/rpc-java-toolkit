package com.carpanese.rpc.android

import com.carpanese.rpc.core.RpcException
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Android RPC client
 */
class RpcClientKtTest {
    
    @Test
    fun testClientCreation() {
        val client = RpcClientKt("http://localhost:3000/rpc")
        assertEquals("http://localhost:3000/rpc", client.url)
        assertFalse(client.safeMode)
        client.close()
    }
    
    @Test
    fun testSafeModeEnabled() {
        val config = com.carpanese.rpc.client.RpcClientConfig().setSafeMode(true)
        val client = RpcClientKt("http://localhost:3000/rpc", config)
        assertTrue(client.safeMode)
        client.close()
    }
    
    @Test
    fun testAuthToken() {
        val client = RpcClientKt("http://localhost:3000/rpc")
        client.setAuthToken("test-token")
        client.clearAuth()
        client.close()
    }
}
