package com.openparty.app.core.analytics.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.analytics.domain.AnalyticsEvent
import com.openparty.app.core.analytics.domain.repository.AnalyticsService
import javax.inject.Inject
import timber.log.Timber

class TrackAppOpenedUseCase @Inject constructor(
    private val analyticsService: AnalyticsService
) {
    suspend operator fun invoke(userId: String?): DomainResult<Unit> {
        return try {
            val event = if (userId != null) {
                AnalyticsEvent.AppOpened(customProperties = mapOf("user_id" to userId))
            } else {
                AnalyticsEvent.AnonymousAppOpened
            }
            analyticsService.trackEvent(event)
            Timber.i("App Opened event tracked with userId: $userId")
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to track App Opened event for userId: $userId")
            DomainResult.Failure(AppError.Analytics.TrackAppOpen)
        }
    }
}
