package com.veroflow.verostay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.veroflow.verostay.data.Apartment
import com.veroflow.verostay.data.MockDataRepository
import com.veroflow.verostay.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    val featured = remember { MockDataRepository.apartments().shuffled().take(8) }
    val cities = remember { MockDataRepository.apartments().map { it.city }.distinct() }

    Scaffold(
        bottomBar = { BottomNavBar(navController, current = "home") }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search apartments or cities") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Home Search Bar" }
                        .then(Modifier.padding(bottom = 16.dp))
                )
            }
            item {
                Card(Modifier.fillMaxWidth().semantics { contentDescription = "Promotional Banner" }) {
                    Text("Summer Sale — up to 20% off select stays", Modifier.padding(16.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                Text("Popular Cities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(Modifier.padding(vertical = 8.dp)) {
                    items(cities) { city ->
                        AssistChip(
                            onClick = { navController.navigate(Routes.searchResults(city)) },
                            label = { Text(city) },
                            modifier = Modifier.padding(end = 8.dp).semantics { contentDescription = "Category $city" }
                        )
                    }
                }
            }
            item {
                Text("Featured Apartments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            items(featured) { apt ->
                ApartmentCard(apt) { navController.navigate(Routes.apartmentDetails(apt.id)) }
            }
        }
    }
}

@Composable
fun ApartmentCard(apt: Apartment, onClick: () -> Unit) {
    var isFav by remember { mutableStateOf(MockDataRepository.isFavorite(apt.id)) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).semantics { contentDescription = "Apartment Card ${apt.id}" },
        onClick = onClick
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Home, contentDescription = "Apartment Image") }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(apt.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${apt.city}, ${apt.country}", style = MaterialTheme.typography.bodySmall)
                Text("$${apt.pricePerNight.toInt()}/night · ★ ${"%.1f".format(apt.rating)} · ${"%.1f".format(apt.distanceKm)} km", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(
                onClick = {
                    MockDataRepository.toggleFavorite(apt.id)
                    isFav = MockDataRepository.isFavorite(apt.id)
                },
                modifier = Modifier.semantics { contentDescription = "Favorite Icon ${apt.id}" }
            ) {
                Icon(if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    var destination by remember { mutableStateOf("") }
    var checkIn by remember { mutableStateOf("") }
    var checkOut by remember { mutableStateOf("") }
    var guestsCount by remember { mutableStateOf(1) }
    var priceRange by remember { mutableStateOf(0f..300f) }
    var propertyType by remember { mutableStateOf("Any") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Search Apartments", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = destination, onValueChange = { destination = it },
            label = { Text("Destination") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Destination Field" }
        )
        Spacer(Modifier.height(8.dp))
        Row {
            OutlinedTextField(
                value = checkIn, onValueChange = { checkIn = it },
                label = { Text("Check-in") }, singleLine = true,
                modifier = Modifier.weight(1f).semantics { contentDescription = "Check-in Date Field" }
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = checkOut, onValueChange = { checkOut = it },
                label = { Text("Check-out") }, singleLine = true,
                modifier = Modifier.weight(1f).semantics { contentDescription = "Check-out Date Field" }
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Guests: $guestsCount")
            IconButton(onClick = { if (guestsCount > 1) guestsCount-- }, modifier = Modifier.semantics { contentDescription = "Decrease Guests" }) { Icon(Icons.Filled.Remove, null) }
            IconButton(onClick = { guestsCount++ }, modifier = Modifier.semantics { contentDescription = "Increase Guests" }) { Icon(Icons.Filled.Add, null) }
        }
        Spacer(Modifier.height(8.dp))
        Text("Price Range: $${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}")
        RangeSlider(
            value = priceRange, onValueChange = { priceRange = it }, valueRange = 0f..500f,
            modifier = Modifier.semantics { contentDescription = "Price Range Slider" }
        )
        var typeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
            OutlinedTextField(
                value = propertyType, onValueChange = {}, readOnly = true,
                label = { Text("Property Type") },
                modifier = Modifier.menuAnchor().fillMaxWidth().semantics { contentDescription = "Property Type Dropdown" }
            )
            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                listOf("Any", "Apartment", "Studio", "Villa", "Loft", "Cottage").forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { propertyType = it; typeExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row {
            OutlinedButton(
                onClick = { destination = ""; checkIn = ""; checkOut = ""; guestsCount = 1; priceRange = 0f..300f; propertyType = "Any" },
                modifier = Modifier.weight(1f).semantics { contentDescription = "Clear Filters Button" }
            ) { Text("Clear Filters") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { navController.navigate(Routes.searchResults(destination.ifBlank { " " })) },
                modifier = Modifier.weight(1f).semantics { contentDescription = "Search Button" }
            ) { Text("Search") }
        }
    }
}

@Composable
fun SearchResultsScreen(navController: NavController, query: String) {
    val results = remember(query) { MockDataRepository.search(query.trim()) }
    var sortBy by remember { mutableStateOf("Recommended") }
    val sorted = remember(results, sortBy) {
        when (sortBy) {
            "Price: Low to High" -> results.sortedBy { it.pricePerNight }
            "Price: High to Low" -> results.sortedByDescending { it.pricePerNight }
            "Rating" -> results.sortedByDescending { it.rating }
            else -> results
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Search Results (${sorted.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        var sortExpanded by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { sortExpanded = true }, modifier = Modifier.semantics { contentDescription = "Sort Button" }) { Text("Sort: $sortBy") }
            DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                listOf("Recommended", "Price: Low to High", "Price: High to Low", "Rating").forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { sortBy = it; sortExpanded = false })
                }
            }
        }
        if (sorted.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.semantics { contentDescription = "Empty Search Results State" }) {
                    Icon(Icons.Filled.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No apartments found")
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { navController.navigate(Routes.SEARCH) { popUpTo(Routes.SEARCH) { inclusive = true } } },
                        modifier = Modifier.semantics { contentDescription = "Clear Filters From Empty State" }
                    ) { Text("Clear Filters") }
                }
            }
        } else {
            LazyColumn {
                items(sorted) { apt ->
                    ApartmentCard(apt) { navController.navigate(Routes.apartmentDetails(apt.id)) }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, current: String) {
    NavigationBar {
        NavigationBarItem(
            selected = current == "home", onClick = { navController.navigate(Routes.HOME) { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Home") },
            modifier = Modifier.semantics { contentDescription = "Bottom Nav Home" }
        )
        NavigationBarItem(
            selected = current == "bookings", onClick = { navController.navigate(Routes.MY_BOOKINGS) { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.CalendarMonth, null) }, label = { Text("Bookings") },
            modifier = Modifier.semantics { contentDescription = "Bottom Nav Bookings" }
        )
        NavigationBarItem(
            selected = current == "favorites", onClick = { navController.navigate(Routes.FAVORITES) { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Favorite, null) }, label = { Text("Favorites") },
            modifier = Modifier.semantics { contentDescription = "Bottom Nav Favorites" }
        )
        NavigationBarItem(
            selected = current == "notifications", onClick = { navController.navigate(Routes.NOTIFICATIONS) { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Notifications, null) }, label = { Text("Notifications") },
            modifier = Modifier.semantics { contentDescription = "Bottom Nav Notifications" }
        )
        NavigationBarItem(
            selected = current == "profile", onClick = { navController.navigate(Routes.PROFILE) { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Person, null) }, label = { Text("Profile") },
            modifier = Modifier.semantics { contentDescription = "Bottom Nav Profile" }
        )
    }
}
