package com.example.eventapp.data

data class Event(
    val id: Int,
    val title: String,
    val date: String,
    val location: String,
    val category: String,
    val imageResId: Int? = null
)
