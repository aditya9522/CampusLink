package com.example.eventapp.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = AppConfig.BASE_URL

    private var _apiService: ApiService? = null
    private var _okHttpClient: OkHttpClient? = null

    fun getOkHttpClient(context: Context): OkHttpClient {
        if (_okHttpClient == null) {
            val tokenManager = TokenManager(context.applicationContext)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            _okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
        return _okHttpClient!!
    }

    fun getInstance(context: Context): ApiService {
        if (_apiService == null) {
            _apiService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
        return _apiService!!
    }
}
