package com.example.movie_project.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.movie_project.R
import com.example.movie_project.databinding.FragmentMessagingBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * A simple [Fragment] subclass.
 * Use the [MessagingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentMessagingBinding.inflate(inflater, container, false)

//        binding.toolbarProfileImage.setOnClickListener{
//            val intent = Intent(activity, ProfileActivity::class.java)
//            startActivity(intent)
//        }


        return binding.root
    }

}