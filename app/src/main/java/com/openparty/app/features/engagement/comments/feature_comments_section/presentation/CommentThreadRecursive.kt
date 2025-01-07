package com.openparty.app.features.engagement.comments.feature_comments_section.presentation

import androidx.compose.runtime.Composable
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment

@Composable
fun CommentThreadRecursive(
    comment: Comment,
    commentMap: Map<String, Comment>,
    indentLevel: Int,
    timeDiffText: String
) {
    CommentItem(comment = comment, indentLevel = indentLevel, timeDiffText = timeDiffText)

    val childComments = commentMap.values.filter { it.parentCommentId == comment.commentId }
    childComments.forEach { childComment ->
        CommentThreadRecursive(
            comment = childComment,
            commentMap = commentMap,
            indentLevel = indentLevel + 1,
            timeDiffText = timeDiffText
        )
    }
}
