package com.openparty.app.features.newsfeed.council_meetings.shared.domain.repository

import androidx.paging.PagingData
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.model.CouncilMeeting
import kotlinx.coroutines.flow.Flow

interface CouncilMeetingRepository {
    fun getCouncilMeetings(): Flow<PagingData<CouncilMeeting>>
    suspend fun getCouncilMeetingById(councilMeetingId: String): DomainResult<CouncilMeeting>
}
