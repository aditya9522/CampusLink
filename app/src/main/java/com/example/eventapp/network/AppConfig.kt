package com.example.eventapp.network

object AppConfig {
    /**
     * The base URL for the backend API.
     * Update this to switch between development and production.
     * 
     * Production: https://campuslink-9wgm.onrender.com/
     * Development: http://10.0.2.2:8000/ (for Android Emulator)
     */
    const val BASE_URL = "https://campuslink-9wgm.onrender.com/"
    
    // WebSocket URL derived from BASE_URL
    val WS_URL = BASE_URL.replace("http", "ws").replace("https", "wss") + "api/v1/ws"
}
