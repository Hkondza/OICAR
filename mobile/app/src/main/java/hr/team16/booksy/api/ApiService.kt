package hr.team16.booksy.api

import hr.team16.booksy.model.*
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // Properties
    @GET("api/properties")
    suspend fun getProperties(
        @Header("Authorization") token: String
    ): List<Property>

    @GET("api/properties/{id}")
    suspend fun getPropertyById(
        @Header("Authorization") token: String,
        @Path("id") id: Long): Property

    // Rooms
    @GET("api/rooms/property/{propertyId}")
    suspend fun getRoomsByProperty(
        @Header("Authorization") token: String,
        @Path("propertyId") propertyId: Long
    ): List<Room>


    @GET("api/rooms/property/{roomId}")
    suspend fun getRoomById(
        @Path("roomId") roomId: Long
    ): Room




    // Reservations
    @POST("api/reservations")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body request: ReservationRequest
    ): ReservationResponse

    @GET("api/reservations/my")
    suspend fun getMyReservations(
        @Header("Authorization") token: String
    ): List<ReservationResponse>


    @GET("api/reservations/room/{roomId}")
    suspend fun getReservationsByRoomId(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: Long
    ): List<ReservationResponse>

    @PATCH("api/reservations/{id}/status")
    suspend fun cancelReservation(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body status: Map<String, String>
    ): ReservationResponse
}