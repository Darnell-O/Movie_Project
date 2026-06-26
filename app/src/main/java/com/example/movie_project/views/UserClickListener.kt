package com.example.movie_project.views

import com.example.movie_project.models.domain.UsersModel

interface UserClickListener {
    fun onUserClicked(user: UsersModel)
}