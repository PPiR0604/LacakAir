package com.example.lacakair.data

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val likes: List<String> = emptyList(),
    val timestamp: Any = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null
) {
    fun getTimestampLong(): Long {
        return when (timestamp) {
            is Long -> timestamp
            is Timestamp -> timestamp.seconds * 1000
            else -> System.currentTimeMillis()
        }
    }

    fun hasLocation(): Boolean {
        return latitude != null && longitude != null
    }
}
