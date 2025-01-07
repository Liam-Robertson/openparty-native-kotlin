package com.openparty.app.features.startup.verification.feature_email_verification.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.openparty.app.core.shared.presentation.ErrorText
import com.openparty.app.core.shared.presentation.RoundedButton
import com.openparty.app.core.shared.presentation.TitleText
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.navigation.NavDestinations
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EmailVerificationScreen(
    navController: NavHostController,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.destination.route) {
                        popUpTo(NavDestinations.EmailVerification.route) { inclusive = true }
                    }
                }
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleText("Verify Your Email")

        Spacer(modifier = Modifier.height(16.dp))

        RoundedButton(onClick = { viewModel.onSendVerificationClick() }, text = "Send Verification Email")

        Spacer(modifier = Modifier.height(16.dp))

        RoundedButton(onClick = { viewModel.onCheckEmailVerificationStatus() }, text = "Check Verification Status")

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(16.dp))

        ErrorText(errorMessage = uiState.errorMessage)
    }
}
