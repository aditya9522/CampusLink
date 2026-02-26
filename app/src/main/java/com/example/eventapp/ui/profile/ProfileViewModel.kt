package com.example.eventapp.ui.profile

import androidx.lifecycle.*
import com.example.eventapp.network.models.UserResponse
import com.example.eventapp.network.models.UserUpdateRequest
import com.example.eventapp.repository.AppRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AppRepository) : ViewModel() {

    private val _user = MutableLiveData<UserResponse?>()
    val user: LiveData<UserResponse?> = _user

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _user.value = repository.getCurrentUser()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateProfile(req: UserUpdateRequest) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _user.value = repository.updateCurrentUser(req)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onDone()
        }
    }

    fun deleteAccount(onDone: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.deleteAccount()
                onDone()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete account")
            } finally {
                _loading.value = false
            }
        }
    }
    fun uploadProfileImage(file: java.io.File) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val updatedUser = repository.uploadProfileImage(file)
                _user.value = updatedUser
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun uploadVerificationID(file: java.io.File, onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.uploadVerificationID(file)
                onDone()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
