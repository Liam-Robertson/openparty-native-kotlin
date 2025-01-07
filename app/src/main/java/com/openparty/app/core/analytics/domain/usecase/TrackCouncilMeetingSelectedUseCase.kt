package com.openparty.app.core.analytics.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.analytics.domain.repository.AnalyticsService
import com.openparty.app.core.analytics.domain.AnalyticsEvent
import javax.inject.Inject
import timber.log.Timber

class TrackCouncilMeetingSelectedUseCase @Inject constructor(
    private val analyticsService: AnalyticsService
) {
    suspend operator fun invoke(councilMeetingId: String): DomainResult<Unit> {
        return try {
            val event = AnalyticsEvent.CouncilMeetingSelected(councilMeetingId)
            analyticsService.trackEvent(event)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to track council meeting selected event for ID: $councilMeetingId")
            DomainResult.Failure(AppError.Analytics.TrackCouncilMeetingPreviewClick)
        }
    }
}
