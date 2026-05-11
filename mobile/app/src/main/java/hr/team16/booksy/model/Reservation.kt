package hr.team16.booksy.model

data class ReservationRequest(
    val roomId: Long,
    val checkIn: String,
    val checkOut: String
)

data class ReservationResponse(
    val id: Long,
    val roomId: Long,
    val roomName: String,
    val propertyName: String,
    val city: String,
    val checkIn: String,
    val checkOut: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val guestEmail: String
)