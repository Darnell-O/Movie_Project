package com.example.movie_project.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.movie_project.databinding.FragmentLeaderboardBinding


class Leaderboard_Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentLeaderboardBinding.inflate(inflater, container, false)

        binding.toolbarLeaderboardActivity.title = "Leader Board"

        return binding.root
    }
}