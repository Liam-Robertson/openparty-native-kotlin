package com.openparty.app.features.engagement.comments.feature_add_comment.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.openparty.app.core.shared.presentation.ErrorText
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.core.shared.presentation.BodyTextInput
import com.openparty.app.core.shared.presentation.TopContainer
import com.openparty.app.navigation.NavDestinations
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddCommentScreen(
    navController: NavController,
    viewModel: AddCommentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val titleText = viewModel.titleText

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    if (event.destination is NavDestinations.Back) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(event.destination.route)
                    }
                }
            }
        }
    }

    ErrorText(uiState.errorMessage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopContainer(
            onBackClicked = { viewModel.onBackClicked() },
            onPostClicked = { viewModel.onPostClicked() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(titleText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        BodyTextInput(
            textFieldValue = uiState.commentText,
            onTextChange = { viewModel.onCommentTextChanged(it) },
            placeholderText = "Enter your comment..."
        )
    }
}
