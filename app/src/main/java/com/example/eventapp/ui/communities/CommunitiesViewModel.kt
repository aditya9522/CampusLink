package com.example.eventapp.ui.communities

import androidx.lifecycle.*
import com.example.eventapp.data.Community
import com.example.eventapp.network.models.CommunityResponse
import com.example.eventapp.repository.AppRepository
import kotlinx.coroutines.launch

class CommunitiesViewModel(private val repository: AppRepository) : ViewModel() {

    private val _communities = MutableLiveData<List<Community>>()
    val communities: LiveData<List<Community>> = _communities

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCommunities()
    }

    fun loadCommunities() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val result = repository.getCommunities()
                _communities.value = result.map { it.toCommunity() }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun CommunityResponse.toCommunity() = Community(
        id          = id,
        name        = name,
        description = description ?: "",
        memberCount = memberCount
    )
}
