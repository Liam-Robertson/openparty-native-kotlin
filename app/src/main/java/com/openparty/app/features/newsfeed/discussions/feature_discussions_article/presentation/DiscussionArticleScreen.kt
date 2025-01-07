package com.openparty.app.features.newsfeed.discussions.feature_discussions_article.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.openparty.app.core.shared.presentation.ErrorText
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.engagement.comments.feature_add_comment.presentation.components.AddCommentFooter
import com.openparty.app.features.engagement.comments.feature_comments_section.presentation.CommentsSection
import com.openparty.app.navigation.NavDestinations
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DiscussionArticleScreen(
    navController: NavHostController,
    viewModel: DiscussionArticleViewModel = hiltViewModel()
) {
    val discussion by viewModel.discussion.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.destination.route) {
                        popUpTo(event.destination.route) { inclusive = true }
                    }
                }
            }
        }
    }

    discussion?.let { currentDiscussion ->
        Scaffold(
            bottomBar = {
                AddCommentFooter(
                    fullyVerified = true,
                    onClick = {
                        navController.navigate(
                            NavDestinations.AddComment(
                                discussionId = currentDiscussion.discussionId,
                                titleText = "Comment on ${currentDiscussion.title}"
                            ).route
                        )
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues).padding(16.dp)
            ) {
                item {
                    Text(
                        text = currentDiscussion.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                item {
                    Text(
                        text = currentDiscussion.contentText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                    )
                }
                item {
                    CommentsSection(
                        modifier = Modifier
                    )
                }
                item {
                    ErrorText(errorMessage = uiState.errorMessage)
                }
            }
        }
    } ?: run {
        ErrorText(errorMessage = uiState.errorMessage)
    }
}
