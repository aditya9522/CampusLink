package com.example.eventapp.network

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import org.json.JSONObject

class WSClient(private val client: OkHttpClient) {

    private var webSocket: WebSocket? = null
    
    private val _messages = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val messages: SharedFlow<JSONObject> = _messages

    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WSClient", "Connected to WebSocket")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WSClient", "Received: $text")
                try {
                    val json = JSONObject(text)
                    _messages.tryEmit(json)
                } catch (e: Exception) {
                    Log.e("WSClient", "Error parsing WS message", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WSClient", "Closing: $code / $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WSClient", "Failure: ${t.message}")
            }
        })
    }

    fun sendMessage(content: String, channel: String = "general") {
        val json = JSONObject()
        json.put("content", content)
        json.put("channel", channel)
        webSocket?.send(json.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
}
