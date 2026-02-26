package com.example.eventapp.repository

import com.example.eventapp.network.ApiService
import com.example.eventapp.network.TokenManager
import com.example.eventapp.network.WSClient
import com.example.eventapp.network.models.*
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import org.json.JSONException

/**
 * Parses a raw error body string like {"detail":"Already registered"} into
 * a human-readable message. Falls back gracefully on plain-text or unexpected formats.
 */
private fun parseErrorBody(raw: String?, fallback: String): String {
    if (raw.isNullOrBlank()) return fallback
    return try {
        val json = JSONObject(raw)
        json.optString("detail").takeIf { it.isNotBlank() }
            ?: json.optString("message").takeIf { it.isNotBlank() }
            ?: raw
    } catch (e: JSONException) {
        raw
    }
}

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
        throw Exception(parseErrorBody(response.errorBody()?.string(), "Login failed"))
    }

    suspend fun register(email: String, password: String, fullName: String): UserResponse {
        val response = api.register(RegisterRequest(email, password, fullName))
        if (response.isSuccessful) return response.body()!!
        throw Exception(parseErrorBody(response.errorBody()?.string(), "Registration failed"))
    }

    suspend fun logout() = tokenManager.clearToken()

    val tokenFlow get() = tokenManager.tokenFlow

    // ─── User ────────────────────────────────────────────────────────────────

    suspend fun getCurrentUser(): UserResponse {
        val response = api.getCurrentUser()
        if (response.isSuccessful) return response.body()!!
        throw Exception(parseErrorBody(response.errorBody()?.string(), "Failed to fetch user"))
    }

    suspend fun updateCurrentUser(req: UserUpdateRequest): UserResponse {
        val response = api.updateCurrentUser(req)
        if (response.isSuccessful) return response.body()!!
        throw Exception(parseErrorBody(response.errorBody()?.string(), "Failed to update profile"))
    }

    suspend fun uploadProfileImage(file: java.io.File): UserResponse {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = api.uploadProfileImage(body)
        if (response.isSuccessful) return response.body()!!
        throw Exception(parseErrorBody(response.errorBody()?.string(), "Failed to upload image"))
    }

    // ─── Events ──────────────────────────────────────────────────────────────

    suspend fun getEvents(): List<EventResponse> {
        val response = api.getEvents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(parseErrorBody(response.errorBody()?.string(), "Failed to fetch events"))
    }

    suspend fun createEvent(req: EventCreateRequest): EventResponse {
        val response = api.createEvent(req)
        if (response.isSuccessful) return response.body()!!
        throw Exception(parseErrorBody(response.errorBody()?.string(), "Failed to create event"))
    }

    suspend fun updateEvent(id: Int, req: EventCreateRequest): EventResponse {
        val response = api.updateEvent(id, req)
        if (response.isSuccessful) return response.body()!!
        throw Exception(response.errorBody()?.string() ?: "Failed to update event")
    }

    suspend fun deleteEvent(id: Int): EventResponse {
        val response = api.deleteEvent(id)
        if (response.isSuccessful) return response.body()!!
        throw Exception(response.errorBody()?.string() ?: "Failed to delete event")
    }

    suspend fun registerForEvent(eventId: Int) {
        val response = api.registerForEvent(eventId)
        if (!response.isSuccessful)
            throw Exception(parseErrorBody(response.errorBody()?.string(), "Failed to register for event"))
    }

    suspend fun uploadEventImage(file: java.io.File): String {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = api.uploadEventImage(body)
        if (response.isSuccessful) return response.body()!!.url
        throw Exception(parseErrorBody(response.errorBody()?.string(), "Failed to upload event image"))
    }

    // ─── Colleges ────────────────────────────────────────────────────────────

    suspend fun getColleges(): List<CollegeResponse> {
        val response = api.getColleges()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch colleges")
    }

    // ... (Clubs, Communities, Travel remain the same)

    // ─── Notifications ───────────────────────────────────────────────────────────

    suspend fun getNotifications(): List<NotificationResponse> {
        val response = api.getNotifications()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch notifications")
    }

    suspend fun markAllNotificationsRead() {
        api.markAllNotificationsRead()
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
            val wsUrl = "${com.example.eventapp.network.AppConfig.WS_URL}/$token"
            wsClient.connect(wsUrl)
        }
    }

    fun disconnectChat() = wsClient.disconnect()

    fun sendMessage(content: String, channel: String = "general") = 
        wsClient.sendMessage(content, channel)

    // ─── Marketplace ─────────────────────────────────────────────────────────

    suspend fun getMarketplaceItems(): List<MarketplaceItemResponse> {
        val response = api.getMarketplaceItems()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception(response.errorBody()?.string() ?: "Failed to fetch marketplace items")
    }

    suspend fun uploadVerificationID(file: java.io.File): Any {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = api.submitVerificationRequest(body)
        if (response.isSuccessful) return response.body()!!
        throw Exception(response.errorBody()?.string() ?: "Failed to upload verification ID")
    }

    suspend fun joinCollege(code: String): Any {
        val response = api.joinCollege(code)
        if (response.isSuccessful) return response.body()!!
        throw Exception(response.errorBody()?.string() ?: "Failed to join college")
    }

    suspend fun deleteAccount() {
        val user = getCurrentUser()
        val response = api.deleteUser(user.id)
        if (!response.isSuccessful)
            throw Exception(response.errorBody()?.string() ?: "Failed to delete account")
        tokenManager.clearToken()
    }
}
