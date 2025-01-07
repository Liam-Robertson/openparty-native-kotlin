package com.openparty.app.features.newsfeed.shared.data.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestorePagingSource<T : Any>(
    private val dataSource: FirebaseNewsfeedDataSource<T>
) : PagingSource<QueryDocumentSnapshot, T>() {

    override suspend fun load(params: LoadParams<QueryDocumentSnapshot>): LoadResult<QueryDocumentSnapshot, T> {
        val collection = dataSource.collectionName
        val startAfterId = params.key?.id
        Timber.d(
            "Loading data from FirestorePagingSource for %s with loadSize: %d and startAfter: %s",
            collection,
            params.loadSize,
            startAfterId
        )

        return try {
            val query = dataSource.getQuery(params.key, params.loadSize)
            val snapshot = query.get().await()

            val queryDocuments = snapshot.documents.filterIsInstance<QueryDocumentSnapshot>()
            val items = queryDocuments.mapNotNull { dataSource.transform(it) }
            val nextKey = queryDocuments.lastOrNull()

            if (items.isEmpty()) {
                Timber.w(
                    "No items loaded for %s (startAfter = %s). Possibly an empty collection or no more data.",
                    collection,
                    startAfterId
                )
            } else {
                Timber.d(
                    "Successfully loaded %d items from %s, next key: %s",
                    items.size,
                    collection,
                    nextKey?.id
                )
            }

            LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            Timber.e(e, "Error loading data in FirestorePagingSource for %s", collection)
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<QueryDocumentSnapshot, T>): QueryDocumentSnapshot? {
        Timber.d("Getting refresh key in FirestorePagingSource for %s", dataSource.collectionName)
        return null
    }
}
