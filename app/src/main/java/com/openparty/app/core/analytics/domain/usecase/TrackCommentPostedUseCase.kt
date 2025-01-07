package com.openparty.app.core.analytics.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.analytics.domain.repository.AnalyticsService
import com.openparty.app.core.analytics.domain.AnalyticsEvent
import javax.inject.Inject
import timber.log.Timber

class TrackCommentPostedUseCase @Inject constructor(
    private val analyticsService: AnalyticsService
) {
    suspend operator fun invoke(commentId: String, discussionId: String, contentText: String): DomainResult<Unit> {
        return try {
            val event = AnalyticsEvent.CommentPosted(commentId, discussionId, contentText)
            analyticsService.trackEvent(event)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to track comment posted event for comment ID: $commentId")
            DomainResult.Failure(AppError.Analytics.TrackCommentPosted)
        }
    }
}
