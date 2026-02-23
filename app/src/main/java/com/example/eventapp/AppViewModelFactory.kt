package com.example.eventapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eventapp.repository.AppRepository
import com.example.eventapp.ui.chat.ChatViewModel
import com.example.eventapp.ui.clubs.ClubsViewModel
import com.example.eventapp.ui.communities.CommunitiesViewModel
import com.example.eventapp.ui.events.EventsViewModel
import com.example.eventapp.ui.profile.ProfileViewModel
import com.example.eventapp.ui.travel.TravelViewModel

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EventsViewModel::class.java)      -> EventsViewModel(repository)
            modelClass.isAssignableFrom(ClubsViewModel::class.java)       -> ClubsViewModel(repository)
            modelClass.isAssignableFrom(CommunitiesViewModel::class.java) -> CommunitiesViewModel(repository)
            modelClass.isAssignableFrom(TravelViewModel::class.java)      -> TravelViewModel(repository)
            modelClass.isAssignableFrom(ProfileViewModel::class.java)     -> ProfileViewModel(repository)
            modelClass.isAssignableFrom(ChatViewModel::class.java)        -> ChatViewModel(repository)
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        } as T
    }
}
