package com.example.movie_project.models.domain

/**
 * Domain model representing an application user.
 *
 * Stored to / read from Firebase Realtime Database (hence the nullable,
 * default-valued fields required for Firebase's no-arg deserialization).
 */
data class UsersModel(
    var email: String? = null,
    var uid: String? = null,
)