package com.oo.skinsync.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oo.skinsync.feature.capture.CaptureScreen
import com.oo.skinsync.feature.history.HistoryScreen
import com.oo.skinsync.feature.profile.ProfileScreen
import com.oo.skinsync.feature.result.ResultScreen
import com.oo.skinsync.feature.wardrobe.WardrobeScreen

/** Single-Activity navigation. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val CAPTURE = "capture"
    const val RESULT = "result"
    const val HISTORY = "history"
    const val WARDROBE = "wardrobe"
}

@Composable
fun SkinSyncNavHost(
    navController: NavHostController = rememberNavController(),
    startViewModel: StartViewModel = hiltViewModel(),
) {
    val seen by startViewModel.seen.collectAsStateWithLifecycle()

    // Wait until the onboarding flag is known before choosing the start route.
    when (seen) {
        null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        else -> NavHost(
            navController = navController,
            startDestination = if (seen == true) Routes.HOME else Routes.ONBOARDING,
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onGetStarted = {
                    startViewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                })
            }
            composable(Routes.HOME) {
                HomePlaceholder(
                    onProfile = { navController.navigate(Routes.PROFILE) },
                    onCapture = { navController.navigate(Routes.CAPTURE) },
                    onResult = { navController.navigate(Routes.RESULT) },
                    onHistory = { navController.navigate(Routes.HISTORY) },
                    onWardrobe = { navController.navigate(Routes.WARDROBE) },
                )
            }
            composable(Routes.CAPTURE) {
                CaptureScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(onScanColors = { navController.navigate(Routes.CAPTURE) })
            }
            composable(Routes.RESULT) { ResultScreen() }
            composable(Routes.HISTORY) { HistoryScreen() }
            composable(Routes.WARDROBE) {
                WardrobeScreen(onCancel = { navController.popBackStack() })
            }
        }
    }
}
