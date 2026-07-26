package com.veroflow.verostay.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.veroflow.verostay.data.Guests
import com.veroflow.verostay.data.MockDataRepository
import com.veroflow.verostay.navigation.Routes
import com.veroflow.verostay.viewmodel.AppViewModel

@Composable
fun ApartmentDetailsScreen(navController: NavController, appViewModel: AppViewModel, apartmentId: String) {
    val apt = remember { MockDataRepository.apartment(apartmentId) } ?: return
    val host = remember { MockDataRepository.host(apt.hostId) }
    var isFav by remember { mutableStateOf(MockDataRepository.isFavorite(apt.id)) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            Modifier.fillMaxWidth().height(220.dp).semantics { contentDescription = "Apartment Hero Image" }
                .clickableSimple { navController.navigate(Routes.imageGallery(apt.id)) },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Photo, contentDescription = null, modifier = Modifier.size(64.dp)) }

        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(apt.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { MockDataRepository.toggleFavorite(apt.id); isFav = MockDataRepository.isFavorite(apt.id) },
                    modifier = Modifier.semantics { contentDescription = "Favorite Button" }
                ) { Icon(if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null) }
                IconButton(onClick = { /* share intent stub */ }, modifier = Modifier.semantics { contentDescription = "Share Button" }) {
                    Icon(Icons.Filled.Share, null)
                }
            }
            Text("${apt.city}, ${apt.country} · ★ ${"%.1f".format(apt.rating)} (${apt.reviews.size} reviews)", style = MaterialTheme.typography.bodyMedium)
            Text("$${apt.pricePerNight.toInt()} / night", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(12.dp))
            Text("Description", fontWeight = FontWeight.Bold)
            Text(apt.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))
            Text("Host", fontWeight = FontWeight.Bold)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp).semantics { contentDescription = "Host Info Row" }
                    .clickableSimple { navController.navigate(Routes.hostProfile(apt.hostId)) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(host?.name ?: "Host")
                    Text("★ ${"%.1f".format(host?.rating ?: 0f)}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row {
                AssistChip(
                    onClick = { navController.navigate(Routes.amenities(apt.id)) },
                    label = { Text("Amenities") },
                    modifier = Modifier.semantics { contentDescription = "View Amenities Button" }
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = { navController.navigate(Routes.mapView(apt.id)) },
                    label = { Text("View on Map") },
                    modifier = Modifier.semantics { contentDescription = "View Map Button" }
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = { navController.navigate(Routes.reviews(apt.id)) },
                    label = { Text("Reviews") },
                    modifier = Modifier.semantics { contentDescription = "View Reviews Button" }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("House Rules", fontWeight = FontWeight.Bold)
            Text("No smoking · No parties · Check-in after 2 PM · Check-out before 11 AM", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    appViewModel.selectedApartmentId = apt.id
                    navController.navigate(Routes.calendar(apt.id))
                },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Book Now Button" }
            ) { Text("Book Now") }

            OutlinedButton(
                onClick = { navController.navigate(Routes.chat(apt.hostId)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { contentDescription = "Contact Host Button" }
            ) { Text("Contact Host") }
        }
    }
}

// Lightweight clickable helper (avoids importing ripple/interaction source boilerplate at each call site)
fun Modifier.clickableSimple(onClick: () -> Unit): Modifier = this.pointerInput(Unit) {
    detectTapGestures(onTap = { onClick() })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGalleryScreen(apartmentId: String) {
    val apt = remember { MockDataRepository.apartment(apartmentId) } ?: return
    val pagerState = rememberPagerState(pageCount = { apt.images.size })
    var zoomed by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().semantics { contentDescription = "Image Gallery Pager" }
        ) { page ->
            Box(
                Modifier.fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onDoubleTap = { zoomed = !zoomed }) }
                    .semantics { contentDescription = "Gallery Image ${apt.images[page]}${if (zoomed) " Zoomed" else ""}" },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Photo, contentDescription = null, modifier = Modifier.size(if (zoomed) 220.dp else 120.dp))
            }
        }
        Text(
            "${pagerState.currentPage + 1} / ${apt.images.size}",
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
        )
    }
}

@Composable
fun MapViewScreen(apartmentId: String) {
    // Implementation note: uses custom accessible View components (not an embedded live
    // Google Maps SDK) so pins and zoom controls remain reachable via the accessibility tree.
    val apt = remember { MockDataRepository.apartment(apartmentId) } ?: return
    var zoomLevel by remember { mutableStateOf(12) }
    val nearby = listOf("Nearby Restaurant" to "restaurant", "City Park" to "park", "Shopping Center" to "mall")

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxWidth().weight(1f).semantics { contentDescription = "Map Canvas Zoom Level $zoomLevel" },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.LocationOn, contentDescription = "Apartment Pin Marker",
                    modifier = Modifier.size(48.dp)
                )
                Text(apt.city)
            }
        }
        Row(Modifier.padding(12.dp)) {
            IconButton(onClick = { zoomLevel++ }, modifier = Modifier.semantics { contentDescription = "Zoom In Button" }) { Icon(Icons.Filled.Add, null) }
            IconButton(onClick = { if (zoomLevel > 1) zoomLevel-- }, modifier = Modifier.semantics { contentDescription = "Zoom Out Button" }) { Icon(Icons.Filled.Remove, null) }
            OutlinedButton(onClick = { /* open directions stub */ }, modifier = Modifier.semantics { contentDescription = "Open Directions Button" }) { Text("Open Directions") }
        }
        Text("Nearby", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        LazyRow(Modifier.padding(16.dp)) {
            items(nearby) { (label, tag) ->
                AssistChip(onClick = {}, label = { Text(label) }, modifier = Modifier.padding(end = 8.dp).semantics { contentDescription = "Nearby $tag" })
            }
        }
    }
}

@Composable
fun AmenitiesScreen(apartmentId: String) {
    val apt = remember { MockDataRepository.apartment(apartmentId) } ?: return
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Amenities", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        items(apt.amenities) { amenity ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp).semantics { contentDescription = "Amenity ${amenity.name}" },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(amenity.name)
            }
        }
    }
}

@Composable
fun CalendarScreen(navController: NavController, appViewModel: AppViewModel, apartmentId: String) {
    var checkIn by remember { mutableStateOf("") }
    var checkOut by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val days = (1..28).toList()
    var selectingCheckOut by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Availability Calendar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Check-in: ${checkIn.ifBlank { "-" }}   Check-out: ${checkOut.ifBlank { "-" }}", modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(Modifier.weight(1f).semantics { contentDescription = "Calendar View" }) {
            items(days.chunked(7)) { week ->
                Row {
                    week.forEach { day ->
                        val label = "Aug $day"
                        TextButton(
                            onClick = {
                                if (!selectingCheckOut) {
                                    checkIn = label; selectingCheckOut = true
                                } else {
                                    if (day <= (checkIn.filter { it.isDigit() }.toIntOrNull() ?: 0)) {
                                        error = "Check-out must be after check-in."
                                    } else {
                                        checkOut = label; selectingCheckOut = false; error = null
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).semantics { contentDescription = "Calendar Day $day" }
                        ) { Text("$day") }
                    }
                }
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                if (checkIn.isBlank() || checkOut.isBlank()) {
                    error = "Please select both check-in and check-out dates."
                } else {
                    appViewModel.checkInDate = checkIn
                    appViewModel.checkOutDate = checkOut
                    navController.navigate(Routes.guestSelection(apartmentId))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { contentDescription = "Confirm Dates Button" }
        ) { Text("Continue") }
    }
}

@Composable
fun GuestSelectionScreen(navController: NavController, appViewModel: AppViewModel, apartmentId: String) {
    var guests by remember { mutableStateOf(appViewModel.guests) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Guests", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        GuestCounterRow("Adults", guests.adults, min = 1) { guests = guests.copy(adults = it) }
        GuestCounterRow("Children", guests.children) { guests = guests.copy(children = it) }
        GuestCounterRow("Infants", guests.infants) { guests = guests.copy(infants = it) }
        GuestCounterRow("Rooms", guests.rooms, min = 1) { guests = guests.copy(rooms = it) }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                appViewModel.guests = guests
                navController.navigate(Routes.bookingSummary(apartmentId))
            },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Confirm Guests Button" }
        ) { Text("Continue") }
    }
}

@Composable
fun GuestCounterRow(label: String, value: Int, min: Int = 0, onChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp).semantics { contentDescription = "$label Counter" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        IconButton(onClick = { if (value > min) onChange(value - 1) }, modifier = Modifier.semantics { contentDescription = "Decrease $label" }) {
            Icon(Icons.Filled.Remove, null)
        }
        Text("$value", modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = { onChange(value + 1) }, modifier = Modifier.semantics { contentDescription = "Increase $label" }) {
            Icon(Icons.Filled.Add, null)
        }
    }
}
