package com.openparty.app.features.newsfeed.discussions.shared.data.repository

import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.discussions.shared.data.domain.repository.DiscussionRepository
import com.openparty.app.features.newsfeed.discussions.shared.domain.model.Discussion
import com.openparty.app.features.newsfeed.shared.data.datasource.FirebaseNewsfeedDataSource
import com.openparty.app.features.newsfeed.shared.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscussionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreRepository<Discussion>(
    dataSource = FirebaseNewsfeedDataSource(
        firestore = firestore,
        collectionName = "discussions",
        orderByField = "upvoteCount",
        transform = { documentSnapshot ->
            try {
                Timber.d("Transforming document snapshot to Discussion object: %s", documentSnapshot.id)
                documentSnapshot.toObject(Discussion::class.java)
            } catch (e: Exception) {
                Timber.e(e, "Error transforming document snapshot to Discussion object: %s", documentSnapshot.id)
                null
            }
        }
    ),
    error = AppError.Discussion.General
), DiscussionRepository {

    override fun getDiscussions(): Flow<PagingData<Discussion>> {
        Timber.d("Fetching discussions from DiscussionRepositoryImpl")
        return getPagedItems()
    }

    override suspend fun getDiscussionById(discussionId: String): DomainResult<Discussion> {
        Timber.d("Fetching discussion by ID: %s from DiscussionRepositoryImpl", discussionId)
        return getItemById(discussionId)
    }

    override suspend fun addDiscussion(discussion: Discussion): DomainResult<Discussion> {
        Timber.d("addDiscussion invoked for discussion: %s", discussion.title)
        return try {
            val document = firestore.collection("discussions").document()
            Timber.d("Generated new document ID: %s for discussion: %s", document.id, discussion.title)
            val discussionWithId = discussion.copy(discussionId = document.id)
            document.set(discussionWithId).await()
            Timber.d("Successfully added discussion with ID: %s", discussionWithId.discussionId)
            DomainResult.Success(discussionWithId)
        } catch (e: Exception) {
            Timber.e(e, "Error adding discussion: %s", discussion.title)
            DomainResult.Failure(AppError.Discussion.General)
        }
    }

}
