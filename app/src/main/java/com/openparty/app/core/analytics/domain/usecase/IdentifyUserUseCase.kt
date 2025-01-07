package com.openparty.app.core.analytics.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.analytics.domain.repository.AnalyticsService
import javax.inject.Inject
import timber.log.Timber

class IdentifyUserUseCase @Inject constructor(
    private val analyticsService: AnalyticsService
) {
    suspend operator fun invoke(userId: String): DomainResult<Unit> {
        return try {
            val currentDistinctId = analyticsService.getDistinctId()
            if (currentDistinctId != userId) {
                analyticsService.identifyUser(
                    userId,
                    mapOf("is_logged_in" to true, "login_date" to System.currentTimeMillis())
                )
                Timber.i("User identified in Mixpanel: $userId")
            } else {
                Timber.i("User already identified in Mixpanel: $userId")
            }
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to identify user: $userId")
            DomainResult.Failure(AppError.Analytics.IdentifyUser)
        }
    }
}
