package com.openparty.app.features.engagement.comments.feature_comments_section.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment

@Composable
fun CommentItem(
    comment: Comment,
    indentLevel: Int,
    timeDiffText: String
) {
    val indentPadding = (indentLevel * 16).dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentPadding, top = 8.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.alpha(0.7f)
        ) {
            Text(
                text = comment.screenName,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeDiffText,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Text(
            text = comment.contentText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
    }
}
