package com.example.eventapp

import android.app.Application
import com.example.eventapp.network.RetrofitClient
import com.example.eventapp.network.TokenManager
import com.example.eventapp.network.WSClient
import com.example.eventapp.repository.AppRepository

class EventApplication : Application() {

    val repository: AppRepository by lazy {
        val api = RetrofitClient.getInstance(this)
        val tokenManager = TokenManager(this)
        val okHttpClient = RetrofitClient.getOkHttpClient(this)
        val wsClient = WSClient(okHttpClient)
        AppRepository(api, tokenManager, wsClient)
    }
}
