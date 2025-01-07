package com.openparty.app.features.newsfeed.council_meetings.shared.data.repository

import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.repository.CouncilMeetingRepository
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.model.CouncilMeeting
import com.openparty.app.features.newsfeed.shared.data.datasource.FirebaseNewsfeedDataSource
import com.openparty.app.features.newsfeed.shared.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouncilMeetingRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore
) : FirestoreRepository<CouncilMeeting>(
    dataSource = FirebaseNewsfeedDataSource(
        firestore = firestore,
        collectionName = "council_meetings",
        orderByField = "upvoteCount",
        transform = { documentSnapshot ->
            try {
                Timber.d("Transforming document snapshot to CouncilMeeting object: %s", documentSnapshot.id)
                documentSnapshot.toObject(CouncilMeeting::class.java)
            } catch (e: Exception) {
                Timber.e(e, "Error transforming document snapshot to CouncilMeeting object: %s", documentSnapshot.id)
                null
            }
        }
    ),
    error = AppError.CouncilMeeting.General
), CouncilMeetingRepository {

    override fun getCouncilMeetings(): Flow<PagingData<CouncilMeeting>> {
        Timber.d("Fetching council meetings from CouncilMeetingRepositoryImpl")
        return getPagedItems()
    }

    override suspend fun getCouncilMeetingById(councilMeetingId: String): DomainResult<CouncilMeeting> {
        Timber.d("Fetching council meeting by ID: %s from CouncilMeetingRepositoryImpl", councilMeetingId)
        return getItemById(councilMeetingId)
    }
}
