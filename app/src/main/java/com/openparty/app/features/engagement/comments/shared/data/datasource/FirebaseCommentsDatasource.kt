package com.openparty.app.features.engagement.comments.shared.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCommentsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommentsDataSource {

    override suspend fun getCommentsForDiscussion(discussionId: String): List<Comment> {
        Timber.d("Fetching comments for discussionId: $discussionId")
        return fetchComments("discussionId", discussionId).also {
            Timber.d("Fetched ${it.size} comments for discussionId: $discussionId")
        }
    }

    override suspend fun getCommentsForCouncilMeeting(councilMeetingId: String): List<Comment> {
        Timber.d("Fetching comments for councilMeetingId: $councilMeetingId")
        return fetchComments("councilMeetingId", councilMeetingId).also {
            Timber.d("Fetched ${it.size} comments for councilMeetingId: $councilMeetingId")
        }
    }

    override suspend fun addComment(comment: Comment) {
        Timber.d("Adding comment: $comment")
        try {
            val docRef = firestore.collection("comments").document()
            val newComment = comment.copy(commentId = docRef.id)
            docRef.set(newComment).await()
            Timber.d("Successfully added comment with ID: ${docRef.id}")
        } catch (e: Exception) {
            Timber.e(e, "Error occurred while adding comment: $comment")
            throw Exception("Failed to add comment", e)
        }
    }

    private suspend fun <T> fetchComments(field: String, value: T): List<Comment> {
        Timber.d("Fetching comments for field: $field with value: $value")
        return try {
            val collectionRef = firestore.collection("comments")
            val query = collectionRef.whereEqualTo(field, value)

            val snapshot = query.get().await()
            if (snapshot.isEmpty) {
                Timber.i("No comments found for $field: $value")
                emptyList()
            } else {
                val comments = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(commentId = doc.id)
                }
                Timber.d("Fetched ${comments.size} comments for $field: $value")
                comments
            }
        } catch (e: Exception) {
            Timber.e(e, "Error occurred while fetching comments for $field: $value")
            throw Exception("Failed to fetch comments for $field: $value", e)
        }
    }
}
