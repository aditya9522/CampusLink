package com.example.eventapp.network.models

import com.google.gson.annotations.SerializedName

// ─── Auth ────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val username: String,   // FastAPI OAuth2 form field
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type")   val tokenType: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String
)

// ─── User ────────────────────────────────────────────────────────────────────

data class UserResponse(
    val id: Int,
    val email: String,
    @SerializedName("full_name")    val fullName: String?,
    @SerializedName("is_active")    val isActive: Boolean,
    @SerializedName("is_superuser") val isSuperuser: Boolean
)

// ─── Event ───────────────────────────────────────────────────────────────────

data class EventResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val location: String?,
    @SerializedName("start_time")    val startTime: String?,
    @SerializedName("end_time")      val endTime: String?,
    @SerializedName("image_url")     val imageUrl: String?,
    @SerializedName("organizer_id")  val organizerId: Int,
    @SerializedName("created_at")    val createdAt: String
)

data class EventCreateRequest(
    val title: String,
    val description: String?,
    val location: String?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time")   val endTime: String?,
    @SerializedName("image_url")  val imageUrl: String?
)

// ─── Club ────────────────────────────────────────────────────────────────────

data class ClubResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val category: String?,
    @SerializedName("logo_url")    val logoUrl: String?,
    @SerializedName("created_at") val createdAt: String?
)

// ─── Community ────────────────────────────────────────────────────────────────

data class CommunityResponse(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("image_url")    val imageUrl: String?
)

// ─── Travel ──────────────────────────────────────────────────────────────────

data class TravelPlanResponse(
    val id: Int,
    val destination: String,
    @SerializedName("date_time")       val dateTime: String,
    val mode: String?,
    @SerializedName("seats_available") val seatsAvailable: Int,
    @SerializedName("organizer_id")    val organizerId: Int
)

data class TravelPlanCreateRequest(
    val destination: String,
    @SerializedName("date_time")       val dateTime: String,
    val mode: String,
    @SerializedName("seats_available") val seatsAvailable: Int
)

// ─── Chat (Message) ──────────────────────────────────────────────────────────

data class MessageResponse(
    val id: Int,
    @SerializedName("sender_id") val senderId: Int,
    val content: String,
    val timestamp: String,
    val channel: String
)
