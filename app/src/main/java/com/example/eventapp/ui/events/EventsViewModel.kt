package com.example.eventapp.ui.events

import androidx.lifecycle.*
import com.example.eventapp.data.Event
import com.example.eventapp.network.models.EventResponse
import com.example.eventapp.repository.AppRepository
import kotlinx.coroutines.launch

class EventsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val result = repository.getEvents()
                _events.value = result.map { it.toEvent() }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun registerForEvent(eventId: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.registerForEvent(eventId)
                onResult(true, "Registered successfully!")
                loadEvents() // Refresh list
            } catch (e: Exception) {
                onResult(false, e.message ?: "Registration failed")
            }
        }
    }

    fun createEvent(
        title: String,
        description: String,
        location: String,
        startTime: String? = null,
        endTime: String? = null,
        imageUrl: String? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.createEvent(
                    com.example.eventapp.network.models.EventCreateRequest(
                        title = title,
                        description = description,
                        location = location,
                        startTime = startTime,
                        endTime = endTime,
                        imageUrl = imageUrl
                    )
                )
                onResult(true, "Event created successfully!")
                loadEvents() // Refresh list
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to create event")
            }
        }
    }

    fun uploadEventImage(file: java.io.File, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val url = repository.uploadEventImage(file)
                onResult(true, url)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to upload image")
            }
        }
    }

    private fun EventResponse.toEvent() = Event(
        id       = id,
        title    = title,
        date     = startTime?.take(10) ?: "TBD",
        location = location ?: "TBD",
        category = "Event"
    )
}
