package com.example.eventapp.data

data class TravelPlan(
    val id: Int,
    val destination: String,
    val date: String,
    val mode: String,
    val seatsAvailable: Int
)
