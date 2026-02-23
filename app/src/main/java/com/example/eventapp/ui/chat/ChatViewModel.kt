package com.example.eventapp.ui.chat

import androidx.lifecycle.*
import com.example.eventapp.data.ChatMessage
import com.example.eventapp.network.models.MessageResponse
import com.example.eventapp.repository.AppRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel(private val repository: AppRepository) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentUserId: Int? = null

    init {
        loadProfileAndConnect()
    }

    private fun loadProfileAndConnect() {
        viewModelScope.launch {
            try {
                val user = repository.getCurrentUser()
                currentUserId = user.id
                
                // Now connect to WS and observe
                observeWsMessages()
                repository.connectToChat()
                
                // Load history
                loadMessages()
            } catch (e: Exception) {
                _error.value = "Failed to initialize chat: ${e.message}"
            }
        }
    }

    private fun observeWsMessages() {
        viewModelScope.launch {
            repository.wsMessages.collectLatest { json ->
                val current = _messages.value.orEmpty().toMutableList()
                val senderId = json.optInt("sender_id")
                val newMessage = ChatMessage(
                    id = json.optInt("id", System.currentTimeMillis().toInt()),
                    senderName = if (senderId == currentUserId) "Me" else "User $senderId",
                    text = json.optString("content"),
                    timestamp = "Just now",
                    isMe = senderId == currentUserId
                )
                current.add(newMessage)
                _messages.postValue(current)
            }
        }
    }

    fun loadMessages(channel: String = "general") {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = repository.getMessages(channel)
                _messages.value = result.map { it.toChatMessage() }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendMessage(content: String, channel: String = "general") {
        repository.sendMessage(content, channel)
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnectChat()
    }

    private fun MessageResponse.toChatMessage() = ChatMessage(
        id = id,
        senderName = if (senderId == currentUserId) "Me" else "User $senderId",
        text = content,
        timestamp = timestamp.takeLast(8),
        isMe = senderId == currentUserId
    )
}
