package com.carpanese.rpc.examples.android

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.carpanese.rpc.android.RpcClientKt
import com.carpanese.rpc.client.RpcClient
import com.carpanese.rpc.client.RpcClientConfig
import com.carpanese.rpc.core.RpcException
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration

class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var endpointInput: EditText
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        endpointInput = EditText(this).apply {
            setSingleLine(true)
            setText(intent.getStringExtra("rpcEndpoint") ?: DEFAULT_ENDPOINT)
            hint = "http://10.0.2.2:3000/api"
        }

        val runButton = Button(this).apply {
            text = "Run RPC Checks"
            setOnClickListener { runChecks(endpointInput.text.toString().trim()) }
        }

        output = TextView(this).apply {
            textSize = 14f
            text = "Enter an RPC endpoint and run the checks."
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            addView(endpointInput, matchWidthWrapHeight())
            addView(runButton, matchWidthWrapHeight())
            addView(output, matchWidthWrapHeight())
        }

        setContentView(ScrollView(this).apply { addView(content) })
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun runChecks(endpoint: String) {
        if (endpoint.isBlank()) {
            output.text = "Endpoint is required."
            return
        }

        output.text = "Running checks against $endpoint..."

        scope.launch {
            val log = StringBuilder()
            try {
                runSafeModeChecks(endpoint, log)
                runStandardModeCheck(endpoint, log)
                output.text = log.appendLine("All checks passed.").toString()
            } catch (error: Exception) {
                output.text = log
                    .appendLine("Check failed:")
                    .appendLine(error.javaClass.simpleName)
                    .appendLine(error.message ?: "No error message")
                    .toString()
            }
        }
    }

    private suspend fun runSafeModeChecks(endpoint: String, log: StringBuilder) {
        RpcClientKt(endpoint, config(safeMode = true)).use { client ->
            val ping = client.call("ping").asString
            require(ping == "pong") { "Expected ping result 'pong', got '$ping'" }
            log.appendLine("ping -> $ping")

            val params = JsonObject().apply {
                addProperty("safePrefix", "S:literal")
                addProperty("datePrefix", "D:literal")
                addProperty("bigintLikeString", "9007199254740993n")
                add("values", JsonArray().apply {
                    add("S:nested")
                    add("D:nested")
                    add("9007199254740994n")
                })
            }
            val echo = client.call("echo", params).asJsonObject
            require(echo["safePrefix"].asString == "S:literal")
            require(echo["datePrefix"].asString == "D:literal")
            require(echo["bigintLikeString"].asString == "9007199254740993n")
            log.appendLine("echo -> marker-like strings preserved")

            val types = client.call("types").asJsonObject
            require(types["isoDateString"].asString == "2026-06-10T12:34:56.000Z")
            require(types["bigintValue"].asString == "9007199254740993n")
            log.appendLine("types -> ISO date and BigInt marker handled")

            try {
                client.call("domainError")
                error("domainError unexpectedly succeeded")
            } catch (error: RpcException) {
                require(error.error.code == -32042)
                require(error.error.data.asJsonObject["reason"].asString == "intentional-test-error")
                log.appendLine("domainError -> custom code and error.data received")
            }
        }
    }

    private suspend fun runStandardModeCheck(endpoint: String, log: StringBuilder) {
        val standardConfig = config(safeMode = false).setHeader("X-RPC-Safe-Enabled", "false")
        withContext(Dispatchers.IO) {
            RpcClient(endpoint, standardConfig).use { client ->
                val ping = client.call("ping", null).asString
                require(ping == "S:pong") { "Expected standard client to receive encoded 'S:pong', got '$ping'" }
                log.appendLine("safe=false -> encoded Safe Mode marker observed")
            }
        }
    }

    private fun config(safeMode: Boolean): RpcClientConfig =
        RpcClientConfig()
            .setSafeMode(safeMode)
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .setWriteTimeout(Duration.ofSeconds(10))

    private fun matchWidthWrapHeight(): ViewGroup.LayoutParams =
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    companion object {
        private const val DEFAULT_ENDPOINT = "http://10.0.2.2:3000/api"
    }
}
