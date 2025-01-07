package com.openparty.app.features.newsfeed.discussions.feature_discussions_preview.presentation

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
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.newsfeed.discussions.feature_add_discussion.presentation.AddDiscussionButton
import com.openparty.app.features.newsfeed.shared.presentation.BaseFeedScreen
import com.openparty.app.navigation.NavDestinations
import com.openparty.app.navigation.NavigationFooter
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DiscussionsPreviewScreen(
    navController: NavHostController,
    viewModel: DiscussionsPreviewViewModel = hiltViewModel()
) {
    val lazyDiscussions = viewModel.discussions.collectAsLazyPagingItems()
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
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                BaseFeedScreen(
                    items = lazyDiscussions,
                    emptyPlaceholder = "No discussions yet..."
                ) { discussion ->
                    discussion?.let {
                        DiscussionCard(discussion = it) {
                            viewModel.onDiscussionSelected(it.discussionId)
                        }
                    }
                }
                AddDiscussionButton {
                    navController.navigate(NavDestinations.AddDiscussion.route)
                }
            }
            ErrorText(errorMessage = uiState.errorMessage)
            NavigationFooter(navController = navController, currentRoute = navController.currentBackStackEntry?.destination?.route)
        }
    }
}
