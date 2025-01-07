package com.openparty.app.core.analytics.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.analytics.domain.repository.AnalyticsService
import com.openparty.app.core.analytics.domain.AnalyticsEvent
import javax.inject.Inject
import timber.log.Timber

class TrackDiscussionSelectedUseCase @Inject constructor(
    private val analyticsService: AnalyticsService
) {
    suspend operator fun invoke(discussionId: String): DomainResult<Unit> {
        return try {
            val event = AnalyticsEvent.DiscussionSelected(discussionId)
            analyticsService.trackEvent(event)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to track discussion selected event for ID: $discussionId")
            DomainResult.Failure(AppError.Analytics.TrackDiscussionsPreviewClick)
        }
    }
}
