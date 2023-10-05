package com.example.movie_project.views

import androidx.databinding.DataBindingComponent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.movie_project.models.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class MessagesViewModel: ViewModel() {
    private val _users = MutableLiveData<List<UsersModel>>()
    val users: LiveData<List<UsersModel>> = _users

    private var userAuth :FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDataBase :DatabaseReference



}