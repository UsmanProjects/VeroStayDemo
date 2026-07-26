package com.veroflow.verostay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.veroflow.verostay.data.Booking
import com.veroflow.verostay.data.BookingStatus
import com.veroflow.verostay.data.MockDataRepository
import com.veroflow.verostay.data.PaymentStatus
import com.veroflow.verostay.navigation.Routes
import com.veroflow.verostay.viewmodel.AppViewModel

@Composable
fun BookingSummaryScreen(navController: NavController, appViewModel: AppViewModel, apartmentId: String) {
    val apt = remember { MockDataRepository.apartment(apartmentId) } ?: return
    val nights = 3 // demo fixed-length stay derived from calendar selection
    val total = apt.pricePerNight * nights
    val taxes = total * 0.08
    val fee = total * 0.05

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Booking Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(apt.name, fontWeight = FontWeight.Bold)
        Text("${appViewModel.checkInDate ?: "-"} → ${appViewModel.checkOutDate ?: "-"}")
        Text("Guests: ${appViewModel.guests.adults} adults, ${appViewModel.guests.children} children, ${appViewModel.guests.infants} infants, ${appViewModel.guests.rooms} rooms")
        Spacer(Modifier.height(16.dp))
        SummaryRow("Total Price", total)
        SummaryRow("Taxes", taxes)
        SummaryRow("Service Fee", fee)
        Divider(Modifier.padding(vertical = 8.dp))
        SummaryRow("Grand Total", total + taxes + fee, bold = true)

        Spacer(Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics { contentDescription = "Agree Booking Policy Checkbox" }
        ) {
            Checkbox(checked = appViewModel.bookingPolicyAccepted, onCheckedChange = { appViewModel.bookingPolicyAccepted = it })
            Text("I agree to the Booking Policy")
        }

        var error by remember { mutableStateOf<String?>(null) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (!appViewModel.bookingPolicyAccepted) {
                    error = "You must agree to the Booking Policy to continue."
                } else {
                    navController.navigate(Routes.payment(apartmentId))
                }
            },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Continue To Payment Button" }
        ) { Text("Continue to Payment") }
    }
}

@Composable
fun SummaryRow(label: String, amount: Double, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, Modifier.weight(1f), fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("$${"%.2f".format(amount)}", fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PaymentScreen(navController: NavController, appViewModel: AppViewModel, apartmentId: String) {
    val apt = remember { MockDataRepository.apartment(apartmentId) } ?: return
    var method by remember { mutableStateOf("Credit Card") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }
    var saveCard by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val nights = 3
    val total = apt.pricePerNight * nights
    val taxes = total * 0.08
    val fee = total * 0.05

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Payment", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Column(Modifier.semantics { contentDescription = "Payment Method Selector" }) {
            listOf("Credit Card", "Debit Card", "Cash", "Digital Wallet").forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = method == m, onClick = { method = m })
                    Text(m)
                }
            }
        }

        if (method == "Credit Card" || method == "Debit Card") {
            PaymentField("Card Number", cardNumber) { cardNumber = it }
            Row {
                PaymentField("Expiry (MM/YY)", expiry, modifier = Modifier.weight(1f)) { expiry = it }
                Spacer(Modifier.width(8.dp))
                PaymentField("CVV", cvv, modifier = Modifier.weight(1f), isPassword = true) { cvv = it }
            }
            PaymentField("Card Holder Name", holderName) { holderName = it }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { contentDescription = "Save Card Checkbox" }) {
                Checkbox(checked = saveCard, onCheckedChange = { saveCard = it })
                Text("Save Card")
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

        Spacer(Modifier.height(16.dp))
        Row {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f).semantics { contentDescription = "Cancel Payment Button" }
            ) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if ((method == "Credit Card" || method == "Debit Card") &&
                        (cardNumber.isBlank() || expiry.isBlank() || cvv.isBlank() || holderName.isBlank())
                    ) {
                        error = "Please complete all payment fields."
                        return@Button
                    }
                    val booking = MockDataRepository.createBooking(
                        apartmentId = apartmentId,
                        checkIn = appViewModel.checkInDate ?: "-",
                        checkOut = appViewModel.checkOutDate ?: "-",
                        guests = appViewModel.guests,
                        total = total, taxes = taxes, fee = fee,
                        cardNumber = cardNumber
                    )
                    appViewModel.lastCreatedBookingId = booking.id
                    if (booking.paymentStatus == PaymentStatus.FAILED) {
                        error = "Payment declined. Please try a different card or method."
                    } else {
                        appViewModel.resetBookingFlow()
                        navController.navigate(Routes.bookingConfirmation(booking.id)) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    }
                },
                modifier = Modifier.weight(1f).semantics { contentDescription = "Pay Button" }
            ) { Text("Pay") }
        }
        Text(
            "Demo: card number ${MockDataRepository.DECLINE_CARD_NUMBER} always declines.",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun PaymentField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), isPassword: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = modifier.padding(vertical = 4.dp).semantics { contentDescription = "$label Field" }
    )
}

@Composable
fun BookingConfirmationScreen(navController: NavController, bookingId: String) {
    val booking = remember { MockDataRepository.booking(bookingId) } ?: return
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = "Success Icon", modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))
        Text("Booking Confirmed!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Booking ID: ${booking.id}", modifier = Modifier.semantics { contentDescription = "Booking ID Text" })
        Spacer(Modifier.height(12.dp))
        Box(Modifier.size(120.dp).semantics { contentDescription = "QR Code" }, contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(100.dp))
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { /* download stub */ }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Download Receipt Button" }) { Text("Download Receipt") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { /* share stub */ }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Share Booking Button" }) { Text("Share Booking") }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate(Routes.bookingDetails(booking.id)) },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "View Booking Button" }
        ) { Text("View Booking") }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = { navController.navigate(Routes.HOME) { popUpTo(0) } },
            modifier = Modifier.semantics { contentDescription = "Return Home Button" }
        ) { Text("Return Home") }
    }
}

@Composable
fun MyBookingsScreen(navController: NavController) {
    val bookings = remember { MockDataRepository.bookings() }
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming" to BookingStatus.UPCOMING, "Completed" to BookingStatus.COMPLETED, "Cancelled" to BookingStatus.CANCELLED)

    Scaffold(bottomBar = { BottomNavBar(navController, current = "bookings") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab, modifier = Modifier.semantics { contentDescription = "Bookings Tabs" }) {
                tabs.forEachIndexed { i, (label, _) ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            val filtered = bookings.filter { it.status == tabs[tab].second }
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No bookings here yet.") }
            } else {
                LazyColumn(Modifier.padding(16.dp)) {
                    items(filtered) { b ->
                        BookingRow(b) { navController.navigate(Routes.bookingDetails(b.id)) }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingRow(b: Booking, onClick: () -> Unit) {
    val apt = MockDataRepository.apartment(b.apartmentId)
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).semantics { contentDescription = "Booking Row ${b.id}" },
        onClick = onClick
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(apt?.name ?: b.apartmentId, fontWeight = FontWeight.Bold)
            Text("${b.checkIn} → ${b.checkOut}", style = MaterialTheme.typography.bodySmall)
            Text("Status: ${b.status}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun BookingDetailsScreen(navController: NavController, bookingId: String) {
    var bookingState by remember { mutableStateOf(MockDataRepository.booking(bookingId)) }
    val current = bookingState ?: return
    val apt = remember { MockDataRepository.apartment(current.apartmentId) }
    val host = apt?.let { MockDataRepository.host(it.hostId) }
    var showCancelDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Booking Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(apt?.name ?: current.apartmentId, fontWeight = FontWeight.Bold)
        Text("Host: ${host?.name ?: "-"}")
        Text("Dates: ${current.checkIn} → ${current.checkOut}")
        Text("Guests: ${current.guests.adults} adults, ${current.guests.children} children")
        Text("Payment Status: ${current.paymentStatus}", modifier = Modifier.semantics { contentDescription = "Payment Status Text" })
        Text("Booking Status: ${current.status}", modifier = Modifier.semantics { contentDescription = "Booking Status Text" })

        Spacer(Modifier.height(24.dp))
        if (current.status == BookingStatus.UPCOMING) {
            OutlinedButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Cancel Booking Button" }
            ) { Text("Cancel Booking") }
            Spacer(Modifier.height(8.dp))
        }
        host?.let {
            OutlinedButton(
                onClick = { navController.navigate(Routes.chat(it.id)) },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Contact Host From Booking Button" }
            ) { Text("Contact Host") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { /* download stub */ }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Download Receipt From Details Button" }) {
            Text("Download Receipt")
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Booking") },
            text = { Text("Are you sure you want to cancel this booking?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        MockDataRepository.cancelBooking(current.id)
                        bookingState = MockDataRepository.booking(current.id)
                        showCancelDialog = false
                    },
                    modifier = Modifier.semantics { contentDescription = "Confirm Cancel Booking Yes" }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false },
                    modifier = Modifier.semantics { contentDescription = "Confirm Cancel Booking No" }
                ) { Text("No") }
            }
        )
    }
}
