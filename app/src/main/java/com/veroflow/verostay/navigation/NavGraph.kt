package com.veroflow.verostay.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.veroflow.verostay.ui.screens.*
import com.veroflow.verostay.viewmodel.AppViewModel

@Composable
fun VeroStayNavGraph(navController: NavHostController, appViewModel: AppViewModel) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.ONBOARDING) { OnboardingScreen(navController) }
        composable(Routes.WELCOME) { WelcomeScreen(navController, appViewModel) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController, appViewModel) }
        composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController) }

        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.SEARCH) { SearchScreen(navController) }
        composable(
            Routes.SEARCH_RESULTS,
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStackEntry ->
            SearchResultsScreen(navController, backStackEntry.arguments?.getString("query") ?: "")
        }

        composable(
            Routes.APARTMENT_DETAILS,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            ApartmentDetailsScreen(navController, appViewModel, backStackEntry.arguments?.getString("apartmentId") ?: "")
        }
        composable(
            Routes.IMAGE_GALLERY,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry -> ImageGalleryScreen(backStackEntry.arguments?.getString("apartmentId") ?: "") }
        composable(
            Routes.MAP_VIEW,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry -> MapViewScreen(backStackEntry.arguments?.getString("apartmentId") ?: "") }
        composable(
            Routes.AMENITIES,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry -> AmenitiesScreen(backStackEntry.arguments?.getString("apartmentId") ?: "") }
        composable(
            Routes.CALENDAR,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry -> CalendarScreen(navController, appViewModel, backStackEntry.arguments?.getString("apartmentId") ?: "") }
        composable(
            Routes.GUEST_SELECTION,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry -> GuestSelectionScreen(navController, appViewModel, backStackEntry.arguments?.getString("apartmentId") ?: "") }

        composable(
            Routes.BOOKING_SUMMARY,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry -> BookingSummaryScreen(navController, appViewModel, backStackEntry.arguments?.getString("apartmentId") ?: "") }
        composable(
            Routes.PAYMENT,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry -> PaymentScreen(navController, appViewModel, backStackEntry.arguments?.getString("apartmentId") ?: "") }
        composable(
            Routes.BOOKING_CONFIRMATION,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry -> BookingConfirmationScreen(navController, backStackEntry.arguments?.getString("bookingId") ?: "") }
        composable(Routes.MY_BOOKINGS) { MyBookingsScreen(navController) }
        composable(
            Routes.BOOKING_DETAILS,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry -> BookingDetailsScreen(navController, backStackEntry.arguments?.getString("bookingId") ?: "") }

        composable(
            Routes.HOST_PROFILE,
            arguments = listOf(navArgument("hostId") { type = NavType.StringType })
        ) { backStackEntry -> HostProfileScreen(navController, backStackEntry.arguments?.getString("hostId") ?: "") }
        composable(
            Routes.CHAT,
            arguments = listOf(navArgument("hostId") { type = NavType.StringType })
        ) { backStackEntry -> ChatScreen(backStackEntry.arguments?.getString("hostId") ?: "") }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(navController) }
        composable(Routes.FAVORITES) { FavoritesScreen(navController) }
        composable(
            Routes.REVIEWS,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry -> ReviewsScreen(backStackEntry.arguments?.getString("apartmentId") ?: "") }

        composable(Routes.PROFILE) { ProfileScreen(navController, appViewModel) }
        composable(Routes.EDIT_PROFILE) { EditProfileScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController, appViewModel) }
        composable(Routes.HELP_SUPPORT) { HelpSupportScreen() }
        composable(Routes.ABOUT) { AboutScreen() }
    }
}
