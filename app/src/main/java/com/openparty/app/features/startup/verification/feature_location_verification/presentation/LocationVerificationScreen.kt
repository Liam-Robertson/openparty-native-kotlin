package com.openparty.app.features.startup.verification.feature_location_verification.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.openparty.app.core.shared.presentation.AppSnackbarHost
import com.openparty.app.core.shared.presentation.ErrorText
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.startup.verification.feature_location_verification.presentation.components.LocationVerificationUiEvent
import com.openparty.app.navigation.NavDestinations
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LocationVerificationScreen(
    navController: NavController,
    viewModel: LocationVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.handleLocationPopupResult(isGranted)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is LocationVerificationUiEvent.Navigate -> {
                    navController.navigate(event.destination.route) {
                        popUpTo(NavDestinations.LocationVerification.route) { inclusive = true }
                    }
                }
                is LocationVerificationUiEvent.RequestPermission -> {
                    requestPermissionLauncher.launch(event.permission)
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.showVerificationDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Verify Your Location") },
                    text = { Text("This app is only for residents of West Lothian. Verify your location to continue.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onVerificationDialogOkClicked() }) {
                            Text("Ok")
                        }
                    }
                )
            }

            if (uiState.showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Enable Location Permissions") },
                    text = { Text("This app requires location permissions. Enable them in your device settings.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onSettingsDialogClicked(context) }) {
                            Text("Settings")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {}) {
                            Text("Cancel")
                        }
                    }
                )
            }

            ErrorText(errorMessage = uiState.errorMessage)

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
