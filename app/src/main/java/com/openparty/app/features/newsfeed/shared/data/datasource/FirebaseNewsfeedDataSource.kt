package com.openparty.app.features.newsfeed.shared.data.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirebaseNewsfeedDataSource<T : Any>(
    private val firestore: FirebaseFirestore,
    val collectionName: String,
    private val orderByField: String,
    val transform: (DocumentSnapshot) -> T?
) {

    fun getQuery(startAfter: QueryDocumentSnapshot?, loadSize: Int): Query {
        val queryDescription = buildString {
            appendLine("Constructing Firestore query:")
            appendLine("  Collection: $collectionName")
            appendLine("  OrderBy: $orderByField (DESC)")
            appendLine("  Limit: $loadSize")
            if (startAfter != null) {
                appendLine("  StartAfter doc ID: ${startAfter.id}")
            }
        }
        Timber.d("getQuery() -> %s", queryDescription)

        return try {
            firestore.collection(collectionName)
                .orderBy(orderByField, Query.Direction.DESCENDING)
                .limit(loadSize.toLong())
                .let { query ->
                    if (startAfter != null) {
                        query.startAfter(startAfter)
                    } else {
                        query
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Error creating query for collection: %s", collectionName)
            throw Exception("Failed to create query for collection: $collectionName. Please try again.", e)
        }
    }

    suspend fun getItemById(itemId: String): T? {
        Timber.d(
            "Fetching item by ID: %s from collection: %s",
            itemId,
            collectionName
        )
        return try {
            val snapshot = firestore.collection(collectionName)
                .document(itemId)
                .get()
                .await()
            Timber.d(
                "Item fetched successfully for ID: %s in collection: %s",
                itemId,
                collectionName
            )
            transform(snapshot)
        } catch (e: Exception) {
            Timber.e(
                e,
                "Error fetching item by ID: %s from collection: %s",
                itemId,
                collectionName
            )
            null
        }
    }
}
