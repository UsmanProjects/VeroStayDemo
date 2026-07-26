package com.veroflow.verostay.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.veroflow.verostay.data.Guests
import com.veroflow.verostay.data.MockDataRepository

/** Holds state that spans multiple screens: the in-progress booking flow and app settings. */
class AppViewModel : ViewModel() {

    // ---- Settings (Screen 28) ----
    var darkMode by mutableStateOf(false)
    var notificationsEnabled by mutableStateOf(true)
    var locationServicesEnabled by mutableStateOf(false)
    var autoLoginEnabled by mutableStateOf(false)
    var language by mutableStateOf("English")
    var rememberMe by mutableStateOf(false)

    // ---- In-progress booking flow state ----
    var selectedApartmentId by mutableStateOf<String?>(null)
    var checkInDate by mutableStateOf<String?>(null)
    var checkOutDate by mutableStateOf<String?>(null)
    var guests by mutableStateOf(Guests())
    var bookingPolicyAccepted by mutableStateOf(false)
    var lastCreatedBookingId by mutableStateOf<String?>(null)

    // ---- Session ----
    var isLoggedIn by mutableStateOf(false)
    var isGuest by mutableStateOf(false)

    fun logout() {
        isLoggedIn = false
        isGuest = false
        MockDataRepository.currentUser = null
    }

    fun resetBookingFlow() {
        selectedApartmentId = null
        checkInDate = null
        checkOutDate = null
        guests = Guests()
        bookingPolicyAccepted = false
    }
}
