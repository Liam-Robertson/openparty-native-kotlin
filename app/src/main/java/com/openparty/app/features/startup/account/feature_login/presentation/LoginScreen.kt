package com.openparty.app.features.startup.account.feature_login.presentation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.openparty.app.features.startup.account.shared.presentation.AccountScreen

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    AccountScreen(
        viewModel = viewModel,
        title = "Login",
        actionText = "Login",
        footerText = "Don't have an account? Register",
        onActionClick = { viewModel.onLoginButtonClick() },
        onFooterClick = { viewModel.onTextFooterClick() },
        uiEvent = viewModel.uiEvent,
        navController = navController
    )
}
