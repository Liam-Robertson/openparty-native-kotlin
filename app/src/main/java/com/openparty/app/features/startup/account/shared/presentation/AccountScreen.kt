package com.openparty.app.features.startup.account.shared.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.openparty.app.core.shared.presentation.ErrorText
import com.openparty.app.core.shared.presentation.TitleText
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.startup.account.shared.presentation.components.EmailForm
import com.openparty.app.features.startup.account.shared.presentation.components.PasswordForm
import com.openparty.app.features.startup.account.shared.presentation.model.AccountUiStateUpdate
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    title: String,
    actionText: String,
    footerText: String,
    onActionClick: () -> Unit,
    onFooterClick: () -> Unit,
    uiEvent: SharedFlow<UiEvent>? = null,
    navController: NavHostController
) {
    val state = viewModel.accountUiState.collectAsState().value

    LaunchedEffect(uiEvent) {
        uiEvent?.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.destination.route) {
                        popUpTo(event.destination.route) { inclusive = true }
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TitleText(text = title)
                Spacer(modifier = Modifier.height(16.dp))
                EmailForm(
                    email = state.email,
                    onEmailChange = { viewModel.updateState(AccountUiStateUpdate.UpdateEmail(it)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                PasswordForm(
                    password = state.password,
                    onPasswordChange = { viewModel.updateState(AccountUiStateUpdate.UpdatePassword(it)) },
                    isPasswordVisible = state.isPasswordVisible,
                    onTogglePasswordVisibility = { viewModel.updateState(AccountUiStateUpdate.TogglePasswordVisibility) },
                    onDone = onActionClick
                )
                ErrorText(errorMessage = state.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = actionText)
                }
                Text(
                    text = footerText,
                    modifier = Modifier.clickable { onFooterClick() }
                )
                if (state.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
