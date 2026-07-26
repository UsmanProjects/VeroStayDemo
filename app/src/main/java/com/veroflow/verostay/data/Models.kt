package com.veroflow.verostay.data

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    val address: String = ""
)

data class Host(
    val id: String,
    val name: String,
    val rating: Float,
    val bio: String,
    val photoRes: Int = 0
)

data class Review(
    val id: String,
    val author: String,
    val rating: Int,
    val comment: String
)

data class Amenity(val name: String, val icon: String)

data class Apartment(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val pricePerNight: Double,
    val rating: Float,
    val distanceKm: Double,
    val propertyType: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val hostId: String,
    val description: String,
    val amenities: List<Amenity>,
    val images: List<String>, // placeholder identifiers, e.g. "apt_001_1"
    val reviews: List<Review>
)

enum class BookingStatus { UPCOMING, COMPLETED, CANCELLED }
enum class PaymentStatus { PAID, FAILED, PENDING }

data class Guests(
    val adults: Int = 1,
    val children: Int = 0,
    val infants: Int = 0,
    val rooms: Int = 1
)

data class Booking(
    val id: String,
    val apartmentId: String,
    val checkIn: String,
    val checkOut: String,
    val guests: Guests,
    val totalPrice: Double,
    val taxes: Double,
    val serviceFee: Double,
    var status: BookingStatus,
    var paymentStatus: PaymentStatus
)

data class ChatMessage(
    val id: String,
    val fromUser: Boolean,
    val text: String,
    val timestamp: Long,
    val isImage: Boolean = false
)

enum class NotificationType { BOOKING_UPDATE, PAYMENT, PROMOTION, REMINDER }

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    var read: Boolean = false,
    val relatedBookingId: String? = null
)
