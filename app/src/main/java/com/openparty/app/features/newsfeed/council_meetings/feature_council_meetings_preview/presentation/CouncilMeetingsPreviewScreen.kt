package com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_preview.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.openparty.app.core.shared.presentation.ErrorText
import com.openparty.app.core.shared.presentation.LoadingScreen
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.newsfeed.shared.presentation.BaseFeedScreen
import com.openparty.app.navigation.NavigationFooter
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CouncilMeetingsPreviewScreen(
    navController: NavHostController,
    viewModel: CouncilMeetingsPreviewViewModel = hiltViewModel()
) {
    val lazyCouncilMeetings = viewModel.councilMeetings.collectAsLazyPagingItems()
    val uiEvent = viewModel.uiEvent
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiEvent) {
        uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.destination.route) {
                        popUpTo(event.destination.route) { inclusive = true }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    BaseFeedScreen(
                        items = lazyCouncilMeetings,
                        emptyPlaceholder = "No council meetings yet..."
                    ) { councilMeeting ->
                        councilMeeting?.let {
                            CouncilMeetingCard(councilMeeting = it) {
                                viewModel.onCouncilMeetingSelected(it.councilMeetingId)
                            }
                        }
                    }
                }
                ErrorText(errorMessage = uiState.errorMessage)
                NavigationFooter(
                    navController = navController,
                    currentRoute = navController.currentBackStackEntry?.destination?.route
                )
            }
        }
    }
}
