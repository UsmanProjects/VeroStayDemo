package com.veroflow.verostay.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.veroflow.verostay.data.MockDataRepository
import com.veroflow.verostay.data.User
import com.veroflow.verostay.navigation.Routes
import com.veroflow.verostay.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.SPLASH) { inclusive = true } }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Home, contentDescription = "App Logo", modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(12.dp))
            Text("VeroStay", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(Modifier.semantics { contentDescription = "Loading Indicator" })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        "Discover Apartments" to "Find the perfect stay in any city.",
        "Book Instantly" to "Reserve your apartment in a few taps.",
        "Manage Easily" to "Track bookings, favorites, and chats in one place."
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(
            onClick = { navController.navigate(Routes.WELCOME) },
            modifier = Modifier.align(Alignment.End).semantics { contentDescription = "Skip Onboarding" }
        ) { Text("Skip") }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(pages[page].first, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(pages[page].second, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage < pages.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    navController.navigate(Routes.WELCOME)
                }
            },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = if (pagerState.currentPage < pages.lastIndex) "Next Button" else "Get Started Button" }
        ) {
            Text(if (pagerState.currentPage < pages.lastIndex) "Next" else "Get Started")
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavController, appViewModel: AppViewModel) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text("Welcome to VeroStay", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Find your next stay, anywhere.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate(Routes.LOGIN) },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Login Button" }
        ) { Text("Login") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { navController.navigate(Routes.REGISTER) },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Register Button" }
        ) { Text("Register") }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = {
                appViewModel.isGuest = true
                navController.navigate(Routes.HOME) { popUpTo(0) }
            },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Continue as Guest Button" }
        ) { Text("Continue as Guest") }
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var subscribePromos by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Create Account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LabeledField("First Name", firstName) { firstName = it }
        LabeledField("Last Name", lastName) { lastName = it }
        LabeledField("Email", email) { email = it }
        LabeledField("Phone Number", phone) { phone = it }
        LabeledField("Password", password, isPassword = true) { password = it }
        LabeledField("Confirm Password", confirmPassword, isPassword = true) { confirmPassword = it }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { contentDescription = "Accept Terms Checkbox" }) {
            Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
            Text("Accept Terms & Conditions")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { contentDescription = "Subscribe Promotions Checkbox" }) {
            Checkbox(checked = subscribePromos, onCheckedChange = { subscribePromos = it })
            Text("Subscribe to Promotions")
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                error = when {
                    firstName.isBlank() || lastName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() -> "All fields are required."
                    !email.contains("@") || !email.contains(".") -> "Invalid email address."
                    password.length < 8 -> "Password must be at least 8 characters."
                    password != confirmPassword -> "Passwords do not match."
                    !acceptTerms -> "You must accept the Terms & Conditions."
                    else -> null
                }
                if (error == null) {
                    MockDataRepository.register(User(firstName, lastName, email, phone, password))
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.REGISTER) { inclusive = true } }
                }
            },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Register Submit Button" }
        ) { Text("Register") }

        TextButton(
            onClick = { navController.navigate(Routes.LOGIN) },
            modifier = Modifier.semantics { contentDescription = "Already Have Account Link" }
        ) { Text("Already have an account? Login") }
    }
}

@Composable
fun LoginScreen(navController: NavController, appViewModel: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.semantics { contentDescription = "Back Button" }) {
            Text("←")
        }
        Text("Login", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LabeledField("Email", email) { email = it }
        LabeledField("Password", password, isPassword = true) { password = it }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { contentDescription = "Remember Me Checkbox" }) {
            Checkbox(checked = appViewModel.rememberMe, onCheckedChange = { appViewModel.rememberMe = it })
            Text("Remember Me")
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                error = when {
                    email.isBlank() || password.isBlank() -> "Email and password are required."
                    !MockDataRepository.login(email, password) -> "Invalid credentials."
                    else -> null
                }
                if (error == null) {
                    appViewModel.isLoggedIn = true
                    appViewModel.isGuest = false
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                }
            },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Login Submit Button" }
        ) { Text("Login") }

        TextButton(
            onClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
            modifier = Modifier.semantics { contentDescription = "Forgot Password Link" }
        ) { Text("Forgot Password?") }
        TextButton(
            onClick = { navController.navigate(Routes.REGISTER) },
            modifier = Modifier.semantics { contentDescription = "Go To Register Link" }
        ) { Text("Don't have an account? Register") }

        Spacer(Modifier.height(24.dp))
        Text("Demo account: demo@verostay.com / Demo@1234", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Reset Password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LabeledField("Email", email) { email = it }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (sent) Text("Reset link sent. Check your email.", color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                error = when {
                    email.isBlank() -> "Email is required."
                    !email.contains("@") -> "Invalid email address."
                    else -> null
                }
                sent = error == null
            },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Send Reset Link Button" }
        ) { Text("Send Reset Link") }
    }
}

@Composable
fun LabeledField(label: String, value: String, isPassword: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).semantics { contentDescription = "$label Field" }
    )
}

