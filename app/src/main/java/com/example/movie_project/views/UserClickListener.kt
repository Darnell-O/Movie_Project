package com.example.movie_project.views

import com.example.movie_project.models.UsersModel

interface UserClickListener {
    fun onUserClicked(user: UsersModel)
}