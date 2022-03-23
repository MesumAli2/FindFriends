package com.mesum.findfriends

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class MapsRepository(
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userLocations : CollectionReference = rootRef.collection("usersLocation")
) {
    suspend fun getResponseFromFirestoreUsingCoroutines() = flow {
        var response = listOf<UserLocation>()
        try {
            response = userLocations.get().await().documents.mapNotNull {
                it.toObject(UserLocation::class.java)
            }
        } catch (exception: Exception) {
            Log.d("LocationFragment", exception.toString())
        }
        emit(response)
    }
}