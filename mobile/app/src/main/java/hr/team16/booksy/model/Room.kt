package hr.team16.booksy.model

import java.math.BigDecimal

data class Room(
    val id: Long,
    val name: String,
    val capacity: Int,
    val pricePerNight: BigDecimal,
    val availableFrom: String,
    val availableTo: String,
    val propertyId: Long,
    val propertyName: String,
    val city: String
)