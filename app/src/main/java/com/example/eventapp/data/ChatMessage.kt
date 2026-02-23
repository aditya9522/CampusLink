package com.example.eventapp.data

data class ChatMessage(
    val id: Int,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isMe: Boolean
)
