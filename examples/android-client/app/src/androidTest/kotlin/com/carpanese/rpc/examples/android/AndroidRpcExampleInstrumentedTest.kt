package com.carpanese.rpc.examples.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.carpanese.rpc.android.RpcClientKt
import com.carpanese.rpc.client.RpcClient
import com.carpanese.rpc.client.RpcClientConfig
import com.carpanese.rpc.client.RpcSafeClient
import com.carpanese.rpc.core.RpcException
import com.carpanese.rpc.core.RpcRequest
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class AndroidRpcExampleInstrumentedTest {
    private val endpoint: String =
        InstrumentationRegistry.getArguments().getString("rpcEndpoint")
            ?: "http://10.0.2.2:3000/api"

    private fun config(safeMode: Boolean = true): RpcClientConfig =
        RpcClientConfig()
            .setSafeMode(safeMode)
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .setWriteTimeout(Duration.ofSeconds(10))

    @Test
    fun coroutineClientCallsSafeEndpoint() = runBlocking {
        RpcClientKt(endpoint, config()).use { client ->
            assertTrue(client.safeMode)
            assertEquals(endpoint, client.url)
            assertEquals("pong", client.call("ping").asString)

            val params = JsonObject().apply {
                addProperty("safePrefix", "S:literal")
                addProperty("datePrefix", "D:literal")
                addProperty("bigintLikeString", "9007199254740993n")
                add("nested", JsonObject().apply {
                    add("array", JsonArray().apply {
                        add("S:nested")
                        add("D:nested")
                        add("9007199254740994n")
                    })
                })
            }
            val echo = client.call("echo", params).asJsonObject
            assertEquals("S:literal", echo["safePrefix"].asString)
            assertEquals("D:literal", echo["datePrefix"].asString)
            assertEquals("9007199254740993n", echo["bigintLikeString"].asString)

            val types = client.call("types").asJsonObject
            assertEquals("2026-06-10T12:34:56.000Z", types["isoDateString"].asString)
            assertEquals("9007199254740993n", types["bigintValue"].asString)
        }
    }

    @Test
    fun javaSafeClientBatchRunsOnAndroidRuntime() {
        RpcSafeClient(endpoint, config()).use { client ->
            val arrayParams = JsonArray().apply {
                add(1)
                add(2)
                add(3)
            }
            assertEquals(6, client.call("sumArray", arrayParams).asInt)

            val batch = client.batch(
                listOf(
                    RpcRequest("ping", null, 101),
                    RpcRequest("missing.method", null, 102),
                    RpcRequest.notification(
                        "notify.record",
                        JsonObject().apply { addProperty("eventName", "android-example-batch") }
                    )
                )
            )

            assertEquals(2, batch.size)
            assertEquals("pong", batch[0].result!!.asString)
            assertTrue(batch[1].isError)
            assertEquals(-32601, batch[1].error!!.code)
        }
    }

    @Test
    fun notificationsAndErrorDataWork() {
        RpcSafeClient(endpoint, config()).use { client ->
            client.call("notify.reset", null)
            client.notify(
                "notify.record",
                JsonObject().apply {
                    addProperty("eventName", "android-example-notify")
                    addProperty("seq", 1)
                }
            )

            val stats = client.call("notify.stats", null).asJsonObject
            assertTrue(stats["count"].asInt >= 1)

            try {
                client.call("domainError", null)
                throw AssertionError("domainError unexpectedly succeeded")
            } catch (error: RpcException) {
                assertEquals(-32042, error.error.code)
                val data = error.error.data.asJsonObject
                assertEquals("intentional-test-error", data["reason"].asString)
                assertEquals("S:error-data-literal", data["markerString"].asString)
                assertEquals("D:error-data-literal", data["dateString"].asString)
                assertEquals("9007199254740993n", data["bigintValue"].asString)
            }
        }
    }

    @Test
    fun safeFalseHeaderKeepsSafeMarkersEncoded() {
        val standardConfig = config(false).setHeader("X-RPC-Safe-Enabled", "false")
        RpcClient(endpoint, standardConfig).use { client ->
            val response = client.call(
                "echo",
                JsonObject().apply { addProperty("plain", "hello") }
            ).asJsonObject
            assertEquals("S:hello", response["plain"].asString)

            assertEquals("S:pong", client.call("ping", null).asString)
        }
    }
}
