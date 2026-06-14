# Android Usage

`rpc-android` adds Kotlin and Android convenience APIs on top of the core RPC client.

For a runnable Android project, see [`examples/android-client`](../examples/android-client). It includes a minimal app plus instrumented checks for Safe Mode HTTP calls, batch requests, notifications, and `error.data`.

## Kotlin Coroutines

```kotlin
import it.carpanese.rpc.android.RpcClientKt
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val client = RpcClientKt("http://api.example.com/rpc")

    fun loadData() {
        lifecycleScope.launch {
            try {
                val result = client.call("getData")
                updateUI(result)
            } catch (e: RpcException) {
                showError(e.message)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }
}
```

## ViewModel And LiveData

```kotlin
import it.carpanese.rpc.android.RpcViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MyViewModel : RpcViewModel("http://api.example.com/rpc") {

    private val _data = MutableLiveData<User>()
    val data: LiveData<User> = _data

    fun loadUser(userId: Int) {
        val params = JsonObject().apply {
            addProperty("userId", userId)
        }

        callRpcAs<User>("getUser", params) { user ->
            _data.value = user
        }
    }
}
```

## Flow API

```kotlin
import it.carpanese.rpc.android.*
import kotlinx.coroutines.flow.*

lifecycleScope.launch {
    rpcResultFlowAs<List<User>>("http://api.example.com/rpc") {
        call("getUsers")
    }.collect { result ->
        when (result) {
            is RpcResult.Loading -> showProgress()
            is RpcResult.Success -> updateUI(result.data)
            is RpcResult.Error -> showError(result.exception.message)
        }
    }
}
```

## Retrofit Integration

```kotlin
import it.carpanese.rpc.android.RpcService
import it.carpanese.rpc.android.RetrofitRpcClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder()
    .baseUrl("http://api.example.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val rpcService = retrofit.create(RpcService::class.java)
val client = RetrofitRpcClient(rpcService, "rpc")

lifecycleScope.launch {
    val result = client.call("myMethod", params)
}
```

## Example Patterns

### Simple API Call

```kotlin
class WeatherActivity : AppCompatActivity() {
    private val client = RpcClientKt("http://api.weather.com/rpc")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val params = JsonObject().apply {
                addProperty("city", "Rome")
            }

            try {
                val weather = client.callAs<Weather>("getWeather", params)
                temperatureText.text = "${weather.temp} deg C"
            } catch (e: RpcException) {
                Toast.makeText(this@WeatherActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

### IoT Device Control

```kotlin
class DeviceControlViewModel : RpcViewModel("http://192.168.1.100:8080") {

    fun toggleLed(ledId: Int, state: Boolean) {
        val params = JsonObject().apply {
            addProperty("ledId", ledId)
            addProperty("state", state)
        }

        callRpc("setLed", params) { result ->
            // LED toggled successfully
        }
    }

    fun readSensors() {
        callRpcAs<SensorData>("readSensors") { data ->
            // Update UI with sensor data
        }
    }
}
```

### Real-Time Updates With Flow

```kotlin
class DashboardFragment : Fragment() {

    private val client = RpcClientKt("http://api.example.com/rpc")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                rpcResultFlowAs<SensorData>(client.url) {
                    call("getSensors")
                }.collect { result ->
                    when (result) {
                        is RpcResult.Success -> updateSensors(result.data)
                        is RpcResult.Error -> showError(result.exception.message)
                    }
                }
                delay(5000)
            }
        }
    }
}
```
