package com.example.eventapp.ui.travel

import androidx.lifecycle.*
import com.example.eventapp.data.TravelPlan
import com.example.eventapp.network.models.TravelPlanResponse
import com.example.eventapp.repository.AppRepository
import kotlinx.coroutines.launch

class TravelViewModel(private val repository: AppRepository) : ViewModel() {

    private val _travelPlans = MutableLiveData<List<TravelPlan>>()
    val travelPlans: LiveData<List<TravelPlan>> = _travelPlans

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadTravelPlans()
    }

    fun loadTravelPlans() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val result = repository.getTravelPlans()
                _travelPlans.value = result.map { it.toTravelPlan() }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun TravelPlanResponse.toTravelPlan() = TravelPlan(
        id             = id,
        destination    = destination,
        date           = dateTime.take(10),
        mode           = mode ?: "Unknown",
        seatsAvailable = seatsAvailable
    )
}
