package hr.team16.booksy.model

data class Property(
    val id: Long,
    val name: String,
    val address: String,
    val city: String,
    val country: String,
    val status: String,
    val ownerEmail: String,
    val imageUrl: String? = null
)