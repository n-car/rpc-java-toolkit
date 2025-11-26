package com.carpanese.rpc.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RpcSafeClient
 */
class RpcSafeClientTest {
    
    @Test
    void testSafeClientInitializesWithUrl() {
        RpcSafeClient client = new RpcSafeClient("http://localhost:3000/api");
        assertNotNull(client);
    }
    
    @Test
    void testSafeClientWithConfig() {
        RpcClientConfig config = new RpcClientConfig()
            .setConnectTimeout(java.time.Duration.ofSeconds(5));
        
        RpcSafeClient client = new RpcSafeClient("http://localhost:3000/api", config);
        assertNotNull(client);
    }
    
    @Test
    void testSafeClientInheritsFromRpcClient() {
        RpcSafeClient client = new RpcSafeClient("http://localhost:3000/api");
        assertTrue(client instanceof RpcClient);
    }
    
    @Test
    void testSafeClientWithNullConfig() {
        // Should create with default safe config
        RpcSafeClient client = new RpcSafeClient("http://localhost:3000/api", null);
        assertNotNull(client);
    }
    
    @Test
    void testSafeClientImplementsAutoCloseable() {
        RpcSafeClient client = new RpcSafeClient("http://localhost:3000/api");
        assertTrue(client instanceof AutoCloseable);
    }
}
