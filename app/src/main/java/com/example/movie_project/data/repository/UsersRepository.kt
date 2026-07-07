package com.example.movie_project.data.repository

import com.example.movie_project.models.domain.UsersModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    fun observeUsers(): Flow<List<UsersModel>> = callbackFlow {
        val ref = database.reference.child("users").child("user")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<UsersModel>()
                for (child in snapshot.children) {
                    val user = child.getValue(UsersModel::class.java) ?: continue
                    list.add(user)
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
