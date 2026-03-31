package com.example.movie_project.util

object ApiKeyProvider {
    init {
        System.loadLibrary("native-lib")
    }

    external fun getApiKey(): String
}
