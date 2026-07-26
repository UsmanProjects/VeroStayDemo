package com.veroflow.verostay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.veroflow.verostay.data.MockDataRepository
import com.veroflow.verostay.navigation.Routes
import com.veroflow.verostay.viewmodel.AppViewModel

@Composable
fun ProfileScreen(navController: NavController, appViewModel: AppViewModel) {
    val user = MockDataRepository.currentUser
    Scaffold(bottomBar = { BottomNavBar(navController, current = "profile") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile Picture", modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(8.dp))
            Text(if (user != null) "${user.firstName} ${user.lastName}" else "Guest User", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(user?.email ?: "-")
            Text(user?.phone ?: "-")

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { navController.navigate(Routes.EDIT_PROFILE) },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Edit Profile Button" }
            ) { Text("Edit Profile") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { /* change password stub */ },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Change Password Button" }
            ) { Text("Change Password") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.navigate(Routes.SETTINGS) },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Go To Settings Button" }
            ) { Text("Settings") }
            Spacer(Modifier.height(8.dp))

            var showLogoutDialog by remember { mutableStateOf(false) }
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Logout Button" }
            ) { Text("Logout") }

            if (showLogoutDialog) {
                LogoutDialog(
                    onConfirm = {
                        appViewModel.logout()
                        showLogoutDialog = false
                        navController.navigate(Routes.WELCOME) { popUpTo(0) }
                    },
                    onDismiss = { showLogoutDialog = false }
                )
            }
        }
    }
}

@Composable
fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Logout") },
        text = { Text("Are you sure you want to logout?") },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.semantics { contentDescription = "Logout Yes Button" }) { Text("Yes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.semantics { contentDescription = "Logout No Button" }) { Text("No") }
        }
    )
}

@Composable
fun EditProfileScreen(navController: NavController) {
    val user = MockDataRepository.currentUser
    var name by remember { mutableStateOf(user?.let { "${it.firstName} ${it.lastName}" } ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var saved by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Row {
            OutlinedButton(onClick = { /* camera stub */ }, modifier = Modifier.weight(1f).semantics { contentDescription = "Edit Profile Camera Button" }) { Text("Camera") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { /* gallery stub */ }, modifier = Modifier.weight(1f).semantics { contentDescription = "Edit Profile Gallery Button" }) { Text("Gallery") }
        }
        Spacer(Modifier.height(12.dp))
        LabeledField("Name", name) { name = it }
        LabeledField("Phone", phone) { phone = it }
        LabeledField("Address", address) { address = it }

        if (saved) Text("Profile updated.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { saved = true },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Save Profile Button" }
        ) { Text("Save") }
    }
}

@Composable
fun SettingsScreen(navController: NavController, appViewModel: AppViewModel) {
    var langExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        SettingSwitchRow("Dark Mode", appViewModel.darkMode) { appViewModel.darkMode = it }
        SettingSwitchRow("Notifications", appViewModel.notificationsEnabled) { appViewModel.notificationsEnabled = it }
        SettingSwitchRow("Location Services", appViewModel.locationServicesEnabled) { appViewModel.locationServicesEnabled = it }
        SettingSwitchRow("Auto Login", appViewModel.autoLoginEnabled) { appViewModel.autoLoginEnabled = it }

        Spacer(Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = { langExpanded = true }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Language Dropdown" }) {
                Text("Language: ${appViewModel.language}")
            }
            DropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }) {
                listOf("English", "Urdu", "Arabic", "Spanish", "French").forEach { lang ->
                    DropdownMenuItem(text = { Text(lang) }, onClick = { appViewModel.language = lang; langExpanded = false })
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { /* privacy stub */ }, modifier = Modifier.semantics { contentDescription = "Privacy Policy Button" }) { Text("Privacy Policy") }
        TextButton(onClick = { /* terms stub */ }, modifier = Modifier.semantics { contentDescription = "Terms And Conditions Button" }) { Text("Terms & Conditions") }
        TextButton(onClick = { navController.navigate(Routes.ABOUT) }, modifier = Modifier.semantics { contentDescription = "About Button" }) { Text("About") }

        Spacer(Modifier.height(8.dp))
        var showLogoutDialog by remember { mutableStateOf(false) }
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Settings Logout Button" }
        ) { Text("Logout") }
        if (showLogoutDialog) {
            LogoutDialog(
                onConfirm = {
                    appViewModel.logout()
                    showLogoutDialog = false
                    navController.navigate(Routes.WELCOME) { popUpTo(0) }
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
    }
}

@Composable
fun SettingSwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).semantics { contentDescription = "$label Switch" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun HelpSupportScreen() {
    val faqs = listOf(
        "How do I cancel a booking?" to "Go to My Bookings > select the booking > Cancel Booking.",
        "How do I contact a host?" to "Open the apartment or booking details and tap Contact Host.",
        "Is my payment information secure?" to "This is a demo app; no real payment data is transmitted."
    )
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Help & Support", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(faqs) { (q, a) ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).semantics { contentDescription = "FAQ Item" }) {
                    Column(Modifier.padding(10.dp)) {
                        Text(q, fontWeight = FontWeight.Bold)
                        Text(a, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { /* contact support stub */ }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Contact Support Button" }) { Text("Contact Support") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { /* report issue stub */ }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Report Issue Button" }) { Text("Report Issue") }
    }
}

@Composable
fun AboutScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("About VeroStay Demo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Application Version: 1.0", modifier = Modifier.semantics { contentDescription = "App Version Text" })
        Text("Build Number: 100", modifier = Modifier.semantics { contentDescription = "Build Number Text" })
        Spacer(Modifier.height(16.dp))
        Text("Developer Information", fontWeight = FontWeight.Bold)
        Text("Built as a demo application for the VeroFlow AI-powered automated Android testing platform.")
    }
}
