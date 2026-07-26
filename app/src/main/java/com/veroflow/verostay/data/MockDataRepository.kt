package com.veroflow.verostay.data

import kotlin.random.Random

/**
 * In-memory mock data source. No backend, no network calls.
 *
 * Testability notes for VeroFlow:
 *  - DECLINE_CARD_NUMBER always fails payment (deterministic negative path).
 *  - Any other well-formed card number succeeds.
 *  - EMPTY_SEARCH_QUERY reliably returns zero search results (deterministic empty state).
 *  - reset() restores the repository to its initial seeded state so repeated
 *    automated test runs start from a known baseline.
 */
object MockDataRepository {

    const val DECLINE_CARD_NUMBER = "4000000000000002"
    const val EMPTY_SEARCH_QUERY = "zzz-no-results-zzz"

    private val cities = listOf(
        "Lahore" to "Pakistan", "Karachi" to "Pakistan", "Islamabad" to "Pakistan",
        "Dubai" to "UAE", "Istanbul" to "Turkey", "London" to "UK",
        "Paris" to "France", "New York" to "USA", "Bangkok" to "Thailand",
        "Barcelona" to "Spain"
    )

    private val propertyTypes = listOf("Apartment", "Studio", "Villa", "Loft", "Cottage")

    private val amenityPool = listOf(
        Amenity("Wi-Fi", "wifi"), Amenity("Parking", "parking"), Amenity("Kitchen", "kitchen"),
        Amenity("Air Conditioning", "ac"), Amenity("Washing Machine", "laundry"),
        Amenity("Swimming Pool", "pool"), Amenity("Gym", "gym"), Amenity("Balcony", "balcony"),
        Amenity("Pet Friendly", "pets")
    )

    private lateinit var _hosts: MutableList<Host>
    private lateinit var _apartments: MutableList<Apartment>
    private lateinit var _bookings: MutableList<Booking>
    private lateinit var _favorites: MutableSet<String>
    private lateinit var _chats: MutableMap<String, MutableList<ChatMessage>> // hostId -> messages
    private lateinit var _notifications: MutableList<AppNotification>
    var currentUser: User? = null

    init {
        reset()
    }

    /** Restores all mock data to a known baseline. Call before each automated test run. */
    fun reset() {
        val rnd = Random(42) // fixed seed -> deterministic mock data across resets

        _hosts = (1..12).map { i ->
            Host(
                id = "host_$i",
                name = listOf("Ayesha Khan", "Bilal Ahmed", "Sara Malik", "David Cohen", "Elena Petrova",
                    "Omar Farooq", "Maria Silva", "John Carter", "Fatima Noor", "Liam O'Brien",
                    "Hana Suzuki", "Ali Raza")[i - 1],
                rating = 3.5f + rnd.nextFloat() * 1.5f,
                bio = "Host of ${rnd.nextInt(1, 20)} properties. Superhost since ${2018 + rnd.nextInt(0, 6)}."
            )
        }.toMutableList()

        _apartments = (1..55).map { i ->
            val (city, country) = cities[i % cities.size]
            val hostId = "host_${(i % 12) + 1}"
            val amenities = amenityPool.shuffled(rnd).take(3 + rnd.nextInt(0, 5))
            Apartment(
                id = "apt_${i.toString().padStart(3, '0')}",
                name = "${propertyTypes[i % propertyTypes.size]} in $city #$i",
                city = city,
                country = country,
                pricePerNight = (40 + rnd.nextInt(0, 260)).toDouble(),
                rating = 3.0f + rnd.nextFloat() * 2.0f,
                distanceKm = rnd.nextDouble(0.5, 25.0),
                propertyType = propertyTypes[i % propertyTypes.size],
                bedrooms = 1 + rnd.nextInt(0, 4),
                bathrooms = 1 + rnd.nextInt(0, 3),
                hostId = hostId,
                description = "A comfortable ${propertyTypes[i % propertyTypes.size].lowercase()} located in the heart of $city, close to major attractions and public transport.",
                amenities = amenities,
                images = (1..4).map { imgIdx -> "apt_${i}_$imgIdx" },
                reviews = (1..rnd.nextInt(0, 6)).map { r ->
                    Review(
                        id = "rev_${i}_$r",
                        author = listOf("Guest_${rnd.nextInt(100, 999)}").first(),
                        rating = rnd.nextInt(3, 6),
                        comment = listOf(
                            "Great stay, would book again!",
                            "Clean and cozy, host was very responsive.",
                            "Good location but a bit noisy at night.",
                            "Exactly as described. Loved it.",
                            "Amenities were great, check-in was smooth."
                        ).random(rnd)
                    )
                }
            )
        }.toMutableList()

        _bookings = mutableListOf(
            Booking("bk_1001", "apt_003", "2026-08-10", "2026-08-14", Guests(2, 0, 0, 1), 480.0, 40.0, 20.0, BookingStatus.UPCOMING, PaymentStatus.PAID),
            Booking("bk_1002", "apt_010", "2026-05-01", "2026-05-05", Guests(1, 0, 0, 1), 320.0, 25.0, 15.0, BookingStatus.COMPLETED, PaymentStatus.PAID),
            Booking("bk_1003", "apt_022", "2026-04-10", "2026-04-12", Guests(2, 1, 0, 1), 200.0, 18.0, 10.0, BookingStatus.CANCELLED, PaymentStatus.FAILED)
        )

        _favorites = mutableSetOf("apt_005", "apt_014")

        _chats = mutableMapOf(
            "host_1" to mutableListOf(
                ChatMessage("m1", false, "Hi! Is the apartment available next weekend?", System.currentTimeMillis() - 100000),
                ChatMessage("m2", true, "Yes, it's available. Would you like me to hold it for you?", System.currentTimeMillis() - 90000)
            )
        )

        _notifications = mutableListOf(
            AppNotification("n1", NotificationType.BOOKING_UPDATE, "Booking Confirmed", "Your booking bk_1001 is confirmed.", false, "bk_1001"),
            AppNotification("n2", NotificationType.PAYMENT, "Payment Received", "Payment for bk_1002 was successful.", true, "bk_1002"),
            AppNotification("n3", NotificationType.PROMOTION, "Summer Sale", "Get 15% off bookings in Barcelona.", false, null),
            AppNotification("n4", NotificationType.REMINDER, "Upcoming Stay", "Your stay at apt_003 starts in 3 days.", false, "bk_1001")
        )

        currentUser = null
    }

    fun hosts(): List<Host> = _hosts
    fun host(id: String): Host? = _hosts.find { it.id == id }
    fun apartments(): List<Apartment> = _apartments
    fun apartment(id: String): Apartment? = _apartments.find { it.id == id }

    fun search(query: String, city: String? = null): List<Apartment> {
        if (query == EMPTY_SEARCH_QUERY) return emptyList()
        return _apartments.filter { apt ->
            (query.isBlank() || apt.name.contains(query, true) || apt.city.contains(query, true)) &&
                (city == null || apt.city.equals(city, true))
        }
    }

    fun favorites(): List<Apartment> = _apartments.filter { it.id in _favorites }
    fun isFavorite(apartmentId: String): Boolean = apartmentId in _favorites
    fun toggleFavorite(apartmentId: String) {
        if (apartmentId in _favorites) _favorites.remove(apartmentId) else _favorites.add(apartmentId)
    }

    fun bookings(): List<Booking> = _bookings
    fun booking(id: String): Booking? = _bookings.find { it.id == id }

    fun createBooking(apartmentId: String, checkIn: String, checkOut: String, guests: Guests, total: Double, taxes: Double, fee: Double, cardNumber: String): Booking {
        val success = cardNumber.trim() != DECLINE_CARD_NUMBER
        val booking = Booking(
            id = "bk_${1000 + _bookings.size + 1}",
            apartmentId = apartmentId,
            checkIn = checkIn,
            checkOut = checkOut,
            guests = guests,
            totalPrice = total,
            taxes = taxes,
            serviceFee = fee,
            status = if (success) BookingStatus.UPCOMING else BookingStatus.CANCELLED,
            paymentStatus = if (success) PaymentStatus.PAID else PaymentStatus.FAILED
        )
        _bookings.add(0, booking)
        if (success) {
            _notifications.add(0, AppNotification(
                id = "n_${_notifications.size + 1}",
                type = NotificationType.BOOKING_UPDATE,
                title = "Booking Confirmed",
                message = "Your booking ${booking.id} is confirmed.",
                relatedBookingId = booking.id
            ))
        }
        return booking
    }

    fun cancelBooking(id: String) {
        _bookings.find { it.id == id }?.status = BookingStatus.CANCELLED
    }

    fun chatMessages(hostId: String): List<ChatMessage> = _chats.getOrPut(hostId) { mutableListOf() }
    fun sendChatMessage(hostId: String, text: String, isImage: Boolean = false) {
        _chats.getOrPut(hostId) { mutableListOf() }.add(
            ChatMessage("m_${System.currentTimeMillis()}", true, text, System.currentTimeMillis(), isImage)
        )
    }

    fun notifications(): List<AppNotification> = _notifications
    fun markNotificationRead(id: String) {
        _notifications.find { it.id == id }?.read = true
    }
    fun clearNotifications() {
        _notifications.clear()
    }

    fun addReview(apartmentId: String, rating: Int, comment: String) {
        val apt = apartment(apartmentId) ?: return
        val idx = _apartments.indexOf(apt)
        val newReview = Review("rev_new_${System.currentTimeMillis()}", "You", rating, comment)
        _apartments[idx] = apt.copy(reviews = apt.reviews + newReview)
    }

    fun register(user: User) {
        currentUser = user
    }

    fun login(email: String, password: String): Boolean {
        // Demo: any registered user, or the seeded demo account
        if (email == "demo@verostay.com" && password == "Demo@1234") {
            currentUser = User("Demo", "User", email, "+92 300 0000000", password)
            return true
        }
        val u = currentUser
        return u != null && u.email == email && u.password == password
    }
}
