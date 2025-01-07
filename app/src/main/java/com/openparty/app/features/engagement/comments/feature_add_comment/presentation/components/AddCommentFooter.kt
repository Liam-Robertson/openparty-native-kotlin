package com.openparty.app.features.engagement.comments.feature_add_comment.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun AddCommentFooter(
    fullyVerified: Boolean,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.07f)
            .background(Color.Black)
            .clickable {
                if (fullyVerified) {
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f)
                .background(Color(0xFF333333), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Add a comment",
                color = Color.LightGray,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
