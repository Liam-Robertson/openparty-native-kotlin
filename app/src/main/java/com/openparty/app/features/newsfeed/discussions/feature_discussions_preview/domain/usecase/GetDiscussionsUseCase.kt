package com.openparty.app.features.newsfeed.discussions.feature_discussions_preview.domain.usecase

import androidx.paging.PagingData
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.discussions.shared.domain.model.Discussion
import com.openparty.app.features.newsfeed.discussions.shared.data.domain.repository.DiscussionRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class GetDiscussionsUseCase @Inject constructor(
    private val repository: DiscussionRepository
) {
    operator fun invoke(): DomainResult<Flow<PagingData<Discussion>>> {
        Timber.d("GetDiscussionsUseCase invoked")
        return try {
            Timber.d("Fetching discussions from repository")
            val discussionsFlow = repository.getDiscussions()
            Timber.d("Successfully fetched discussions flow")
            DomainResult.Success(discussionsFlow)
        } catch (e: Exception) {
            Timber.e(e, "Error occurred while fetching discussions")
            DomainResult.Failure(AppError.Discussion.FetchDiscussions)
        }
    }
}
