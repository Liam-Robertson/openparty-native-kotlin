package com.openparty.app.features.newsfeed.discussions.feature_add_discussion.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun AddDiscussionButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .size(90.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Discussion",
                modifier = Modifier.size(45.dp)
            )
        }
    }
}
