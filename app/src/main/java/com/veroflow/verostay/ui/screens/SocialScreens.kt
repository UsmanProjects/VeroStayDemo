package com.veroflow.verostay.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.veroflow.verostay.data.AppNotification
import com.veroflow.verostay.data.ChatMessage
import com.veroflow.verostay.data.MockDataRepository
import com.veroflow.verostay.data.NotificationType
import com.veroflow.verostay.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun HostProfileScreen(navController: NavController, hostId: String) {
    val host = remember { MockDataRepository.host(hostId) } ?: return
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.AccountCircle, contentDescription = "Host Photo", modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(8.dp))
        Text(host.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("★ ${"%.1f".format(host.rating)}")
        Spacer(Modifier.height(8.dp))
        Text(host.bio, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Row {
            Button(
                onClick = { navController.navigate(Routes.chat(host.id)) },
                modifier = Modifier.weight(1f).semantics { contentDescription = "Chat With Host Button" }
            ) { Text("Chat") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = { /* dial intent stub */ },
                modifier = Modifier.weight(1f).semantics { contentDescription = "Call Host Button" }
            ) { Text("Call") }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(hostId: String) {
    val host = remember { MockDataRepository.host(hostId) }
    var messages by remember { mutableStateOf(MockDataRepository.chatMessages(hostId)) }
    var input by remember { mutableStateOf("") }
    var selectedMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        Text(
            host?.name ?: "Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            items(messages) { msg ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.semantics { contentDescription = "Chat Message ${msg.id}" }
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { selectedMessage = msg }
                            )
                    ) {
                        Text(if (msg.isImage) "[Image attachment]" else msg.text, Modifier.padding(10.dp))
                    }
                }
            }
        }

        selectedMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { selectedMessage = null },
                title = { Text("Message Options") },
                text = { Text("Choose an action") },
                confirmButton = {
                    TextButton(onClick = { selectedMessage = null }, modifier = Modifier.semantics { contentDescription = "Copy Message Option" }) { Text("Copy") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { selectedMessage = null }, modifier = Modifier.semantics { contentDescription = "Forward Message Option" }) { Text("Forward") }
                        TextButton(
                            onClick = {
                                messages = messages.filter { it.id != msg.id }
                                selectedMessage = null
                            },
                            modifier = Modifier.semantics { contentDescription = "Delete Message Option" }
                        ) { Text("Delete") }
                    }
                }
            )
        }

        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { MockDataRepository.sendChatMessage(hostId, "[Camera photo]", true); messages = MockDataRepository.chatMessages(hostId) },
                modifier = Modifier.semantics { contentDescription = "Chat Camera Button" }
            ) { Icon(Icons.Filled.CameraAlt, null) }
            IconButton(
                onClick = { MockDataRepository.sendChatMessage(hostId, "[Gallery photo]", true); messages = MockDataRepository.chatMessages(hostId) },
                modifier = Modifier.semantics { contentDescription = "Chat Gallery Button" }
            ) { Icon(Icons.Filled.Image, null) }
            IconButton(
                onClick = { MockDataRepository.sendChatMessage(hostId, "[Attachment]"); messages = MockDataRepository.chatMessages(hostId) },
                modifier = Modifier.semantics { contentDescription = "Chat Attachment Button" }
            ) { Icon(Icons.Filled.AttachFile, null) }
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                placeholder = { Text("Message") },
                modifier = Modifier.weight(1f).semantics { contentDescription = "Chat Input Field" }
            )
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        MockDataRepository.sendChatMessage(hostId, input)
                        messages = MockDataRepository.chatMessages(hostId)
                        input = ""
                        scope.launch { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }
                    }
                },
                modifier = Modifier.semantics { contentDescription = "Send Message Button" }
            ) { Icon(Icons.Filled.Send, null) }
        }
    }
}

@Composable
fun NotificationsScreen(navController: NavController) {
    var notifications by remember { mutableStateOf(MockDataRepository.notifications()) }

    Scaffold(bottomBar = { BottomNavBar(navController, current = "notifications") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                TextButton(
                    onClick = { MockDataRepository.clearNotifications(); notifications = MockDataRepository.notifications() },
                    modifier = Modifier.semantics { contentDescription = "Clear All Notifications Button" }
                ) { Text("Clear All") }
            }
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No notifications.") }
            } else {
                LazyColumn {
                    items(notifications) { n ->
                        NotificationRow(n, navController) {
                            MockDataRepository.markNotificationRead(n.id)
                            notifications = MockDataRepository.notifications()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationRow(n: AppNotification, navController: NavController, onOpen: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).semantics { contentDescription = "Notification Row ${n.id}" },
        onClick = {
            onOpen()
            // Deep-link-style navigation: jump straight to Booking Details from a notification,
            // skipping the normal Home -> Bookings -> Details path.
            n.relatedBookingId?.let { navController.navigate(Routes.bookingDetails(it)) }
        }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                when (n.type) {
                    NotificationType.BOOKING_UPDATE -> Icons.Filled.CalendarMonth
                    NotificationType.PAYMENT -> Icons.Filled.Payment
                    NotificationType.PROMOTION -> Icons.Filled.LocalOffer
                    NotificationType.REMINDER -> Icons.Filled.Alarm
                },
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(n.title, fontWeight = if (n.read) FontWeight.Normal else FontWeight.Bold)
                Text(n.message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun FavoritesScreen(navController: NavController) {
    val favorites = remember { MockDataRepository.favorites() }
    Scaffold(bottomBar = { BottomNavBar(navController, current = "favorites") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Favorites", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (favorites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No favorites yet.") }
            } else {
                LazyColumn {
                    items(favorites) { apt ->
                        ApartmentCard(apt) { navController.navigate(Routes.apartmentDetails(apt.id)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewsScreen(apartmentId: String) {
    val apt = remember { mutableStateOf(MockDataRepository.apartment(apartmentId)) }
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Reviews", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        LazyColumn(Modifier.weight(1f)) {
            items(apt.value?.reviews ?: emptyList()) { r ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).semantics { contentDescription = "Review ${r.id}" }) {
                    Column(Modifier.padding(10.dp)) {
                        Text("${r.author} · ★ ${r.rating}", fontWeight = FontWeight.Bold)
                        Text(r.comment, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Text("Your Rating: $rating", modifier = Modifier.padding(top = 8.dp))
        Row(modifier = Modifier.semantics { contentDescription = "Rating Bar" }) {
            (1..5).forEach { i ->
                IconButton(onClick = { rating = i }, modifier = Modifier.semantics { contentDescription = "Rate $i Stars" }) {
                    Icon(if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder, contentDescription = null)
                }
            }
        }
        OutlinedTextField(
            value = comment, onValueChange = { comment = it },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Review Comment Field" }
        )
        Button(
            onClick = {
                MockDataRepository.addReview(apartmentId, rating, comment)
                apt.value = MockDataRepository.apartment(apartmentId)
                comment = ""
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { contentDescription = "Submit Review Button" }
        ) { Text("Submit Review") }
    }
}
