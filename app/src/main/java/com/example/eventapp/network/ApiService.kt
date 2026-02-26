package com.example.eventapp.network

import com.example.eventapp.network.models.*
import okhttp3.FormBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ────────────────────────────────────────────────────────────────

    @FormUrlEncoded
    @POST("api/v1/login/access-token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    @POST("api/v1/users/")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    // ─── User ────────────────────────────────────────────────────────────────

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("api/v1/users/me")
    suspend fun updateCurrentUser(@Body request: UserUpdateRequest): Response<UserResponse>

    @Multipart
    @POST("api/v1/users/me/profile-image")
    suspend fun uploadProfileImage(@Part file: okhttp3.MultipartBody.Part): Response<UserResponse>

    @POST("api/v1/users/join-college")
    suspend fun joinCollege(@Query("invite_code") code: String): Response<Any>

    @DELETE("api/v1/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Any>

    // ─── Events ──────────────────────────────────────────────────────────────

    @GET("api/v1/events/")
    suspend fun getEvents(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<EventResponse>>

    @GET("api/v1/events/{id}")
    suspend fun getEvent(@Path("id") id: Int): Response<EventResponse>

    @POST("api/v1/events/")
    suspend fun createEvent(@Body event: EventCreateRequest): Response<EventResponse>

    @PUT("api/v1/events/{id}")
    suspend fun updateEvent(@Path("id") id: Int, @Body event: EventCreateRequest): Response<EventResponse>

    @DELETE("api/v1/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): Response<EventResponse>

    @POST("api/v1/events/{id}/register")
    suspend fun registerForEvent(@Path("id") id: Int): Response<Any>

    @Multipart
    @POST("api/v1/events/upload-image")
    suspend fun uploadEventImage(@Part file: okhttp3.MultipartBody.Part): Response<ImageUrlResponse>

    // ─── Colleges ────────────────────────────────────────────────────────────

    @GET("api/v1/colleges/")
    suspend fun getColleges(): Response<List<CollegeResponse>>

    // ─── Clubs ───────────────────────────────────────────────────────────────

    @GET("api/v1/clubs/")
    suspend fun getClubs(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<ClubResponse>>

    // ─── Communities ─────────────────────────────────────────────────────────

    @GET("api/v1/communities/")
    suspend fun getCommunities(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<CommunityResponse>>

    // ─── Travel ──────────────────────────────────────────────────────────────

    @GET("api/v1/travel/")
    suspend fun getTravelPlans(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<TravelPlanResponse>>

    @POST("api/v1/travel/")
    suspend fun createTravelPlan(@Body plan: TravelPlanCreateRequest): Response<TravelPlanResponse>

    // ─── Chat ────────────────────────────────────────────────────────────────

    @GET("api/v1/chat/{channel}")
    suspend fun getMessages(
        @Path("channel") channel: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<List<MessageResponse>>

    // ─── Notifications ───────────────────────────────────────────────────────────

    @GET("api/v1/notifications/")
    suspend fun getNotifications(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<NotificationResponse>>

    @POST("api/v1/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<Any>

    @GET("api/v1/marketplace/")
    suspend fun getMarketplaceItems(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<MarketplaceItemResponse>>

    @Multipart
    @POST("api/v1/verifications/request")
    suspend fun submitVerificationRequest(
        @Part file: okhttp3.MultipartBody.Part
    ): Response<Any>
}
