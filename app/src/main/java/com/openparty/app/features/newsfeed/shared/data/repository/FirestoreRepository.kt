package com.openparty.app.features.newsfeed.shared.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.shared.data.datasource.FirebaseNewsfeedDataSource
import com.openparty.app.features.newsfeed.shared.data.datasource.FirestorePagingSource
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

abstract class FirestoreRepository<T : Any>(
    private val dataSource: FirebaseNewsfeedDataSource<T>,
    private val error: AppError
) {
    fun getPagedItems(): Flow<PagingData<T>> {
        Timber.d("Fetching paged items from FirestoreRepository for %s", dataSource.collectionName)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                Timber.d("Creating FirestorePagingSource for %s", dataSource.collectionName)
                FirestorePagingSource(dataSource)
            }
        ).flow
    }

    suspend fun getItemById(itemId: String): DomainResult<T> {
        Timber.d(
            "Fetching item by ID: %s in FirestoreRepository for %s",
            itemId,
            dataSource.collectionName
        )
        return try {
            val item = dataSource.getItemById(itemId)
            if (item != null) {
                Timber.d("Item found for ID: %s in %s", itemId, dataSource.collectionName)
                DomainResult.Success(item)
            } else {
                Timber.w("Item not found for ID: %s in %s", itemId, dataSource.collectionName)
                DomainResult.Failure(error)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching item by ID: %s in %s", itemId, dataSource.collectionName)
            DomainResult.Failure(error)
        }
    }
}
