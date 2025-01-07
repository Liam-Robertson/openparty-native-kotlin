package com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_preview.domain.usecase

import androidx.paging.PagingData
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.repository.CouncilMeetingRepository
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.model.CouncilMeeting
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class GetCouncilMeetingsUseCase @Inject constructor(
    private val repository: CouncilMeetingRepository
) {
    operator fun invoke(): DomainResult<Flow<PagingData<CouncilMeeting>>> {
        Timber.d("GetCouncilMeetingsUseCase invoked")
        return try {
            Timber.d("Fetching council meetings from repository")
            val councilMeetingsFlow = repository.getCouncilMeetings()
            Timber.d("Successfully fetched council meetings flow")
            DomainResult.Success(councilMeetingsFlow)
        } catch (e: Exception) {
            Timber.e(e, "Error occurred while fetching council meetings")
            DomainResult.Failure(AppError.CouncilMeeting.FetchCouncilMeetings)
        }
    }
}
