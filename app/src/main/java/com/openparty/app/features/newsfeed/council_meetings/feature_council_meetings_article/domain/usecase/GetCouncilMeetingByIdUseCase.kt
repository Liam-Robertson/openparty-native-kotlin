package com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_article.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.repository.CouncilMeetingRepository
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.model.CouncilMeeting
import timber.log.Timber
import javax.inject.Inject

class GetCouncilMeetingByIdUseCase @Inject constructor(
    private val councilMeetingRepository: CouncilMeetingRepository
) {
    suspend operator fun invoke(councilMeetingId: String): DomainResult<CouncilMeeting> {
        Timber.d("GetCouncilMeetingByIdUseCase invoked for ID: %s", councilMeetingId)
        return try {
            when (val result = councilMeetingRepository.getCouncilMeetingById(councilMeetingId)) {
                is DomainResult.Success -> {
                    Timber.d("Successfully fetched council meeting for ID: %s", councilMeetingId)
                    DomainResult.Success(result.data)
                }
                is DomainResult.Failure -> {
                    Timber.e("Failed to fetch council meeting for ID: %s", councilMeetingId)
                    DomainResult.Failure(AppError.CouncilMeeting.FetchCouncilMeetings)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error while fetching council meeting for ID: %s", councilMeetingId)
            DomainResult.Failure(AppError.CouncilMeeting.FetchCouncilMeetings)
        }
    }
}
