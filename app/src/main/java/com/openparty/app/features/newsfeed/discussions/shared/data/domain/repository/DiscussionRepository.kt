package com.openparty.app.features.newsfeed.discussions.shared.data.domain.repository

import androidx.paging.PagingData
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.features.newsfeed.discussions.shared.domain.model.Discussion
import kotlinx.coroutines.flow.Flow

interface DiscussionRepository {
    fun getDiscussions(): Flow<PagingData<Discussion>>
    suspend fun getDiscussionById(discussionId: String): DomainResult<Discussion>
    suspend fun addDiscussion(discussion: Discussion): DomainResult<Discussion>
}
