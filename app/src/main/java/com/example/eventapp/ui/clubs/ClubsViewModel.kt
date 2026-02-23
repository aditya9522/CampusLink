package com.example.eventapp.ui.clubs

import androidx.lifecycle.*
import com.example.eventapp.data.Club
import com.example.eventapp.network.models.ClubResponse
import com.example.eventapp.repository.AppRepository
import kotlinx.coroutines.launch

class ClubsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _clubs = MutableLiveData<List<Club>>()
    val clubs: LiveData<List<Club>> = _clubs

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadClubs()
    }

    fun loadClubs() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val result = repository.getClubs()
                _clubs.value = result.map { it.toClub() }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun ClubResponse.toClub() = Club(
        id          = id,
        name        = name,
        category    = category ?: "General",
        description = description ?: ""
    )
}
