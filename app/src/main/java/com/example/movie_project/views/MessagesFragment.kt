package com.example.movie_project.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.movie_project.UsersActivity
import com.example.movie_project.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentMessagesBinding.inflate(inflater, container, false)

        val intent = Intent(activity, UsersActivity::class.java)
        startActivity(intent)

        return binding.root


    }

}