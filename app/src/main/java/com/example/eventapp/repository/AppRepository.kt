package com.example.eventapp.repository

import com.example.eventapp.network.ApiService
import com.example.eventapp.network.TokenManager
import com.example.eventapp.network.WSClient
import com.example.eventapp.network.models.*
import kotlinx.coroutines.flow.first

/**
 * Single repository used by all ViewModels.
 * All functions return the parsed body directly or throw on error.
 */
class AppRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val wsClient: WSClient
) {

    // ─── Auth ────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): TokenResponse {
        val response = api.login(email, password)
        if (response.isSuccessful) {
            val token = response.body()!!
            tokenManager.saveToken(token.accessToken)
            return token
        }
        throw Exception(response.errorBody()?.string() ?: "Login failed")
    }

    suspend fun register(email: String, password: String, fullName: String): UserResponse {
        val response = api.register(RegisterRequest(email, password, fullName))
        if (response.isSuccessful) return response.body()!!
        throw Exception(response.errorBody()?.string() ?: "Registration failed")
    }

    suspend fun logout() = tokenManager.clearToken()

    val tokenFlow get() = tokenManager.tokenFlow

    // ─── User ────────────────────────────────────────────────────────────────

    suspend fun getCurrentUser(): UserResponse {
        val response = api.getCurrentUser()
        if (response.isSuccessful) return response.body()!!
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch user")
    }

    // ─── Events ──────────────────────────────────────────────────────────────

    suspend fun getEvents(): List<EventResponse> {
        val response = api.getEvents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch events")
    }

    suspend fun createEvent(req: EventCreateRequest): EventResponse {
        val response = api.createEvent(req)
        if (response.isSuccessful) return response.body()!!
        throw Exception(response.errorBody()?.string() ?: "Failed to create event")
    }

    suspend fun registerForEvent(eventId: Int) {
        val response = api.registerForEvent(eventId)
        if (!response.isSuccessful)
            throw Exception(response.errorBody()?.string() ?: "Failed to register")
    }

    // ─── Clubs ───────────────────────────────────────────────────────────────

    suspend fun getClubs(): List<ClubResponse> {
        val response = api.getClubs()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch clubs")
    }

    // ─── Communities ─────────────────────────────────────────────────────────

    suspend fun getCommunities(): List<CommunityResponse> {
        val response = api.getCommunities()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch communities")
    }

    // ─── Travel ──────────────────────────────────────────────────────────────

    suspend fun getTravelPlans(): List<TravelPlanResponse> {
        val response = api.getTravelPlans()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch travel plans")
    }

    suspend fun createTravelPlan(req: TravelPlanCreateRequest): TravelPlanResponse {
        val response = api.createTravelPlan(req)
        if (response.isSuccessful) return response.body()!!
        throw Exception(response.errorBody()?.string() ?: "Failed to create travel plan")
    }

    // ─── Chat ────────────────────────────────────────────────────────────────

    suspend fun getMessages(channel: String): List<MessageResponse> {
        val response = api.getMessages(channel)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch messages")
    }

    // ─── Real-time ───────────────────────────────────────────────────────────

    val wsMessages get() = wsClient.messages

    suspend fun connectToChat() {
        val token = tokenManager.tokenFlow.first()
        if (token != null) {
            // Android emulator uses 10.0.2.2 for localhost
            val wsUrl = "ws://10.0.2.2:8000/api/v1/ws/$token"
            wsClient.connect(wsUrl)
        }
    }

    fun disconnectChat() = wsClient.disconnect()

    fun sendMessage(content: String, channel: String = "general") = 
        wsClient.sendMessage(content, channel)
}
