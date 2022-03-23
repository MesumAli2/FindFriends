package com.mesum.findfriends

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

class LocationViewModel(var repository: MapsRepository = MapsRepository()) : ViewModel() {
 /*   val responseLiveData = liveData(Dispatchers.IO) {
        emit(repository.getResponseFromFirestoreUsingCoroutines())
    }*/
    fun getResponseUserLocationFlow() = liveData(Dispatchers.IO) {
        repository.getResponseFromFirestoreUsingCoroutines().collect { response ->
            emit(response)
        }
    }




}