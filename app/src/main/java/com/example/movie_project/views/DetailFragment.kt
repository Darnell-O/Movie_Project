package com.example.movie_project.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.movie_project.R

class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)

//        val args: DetailFragmentArgs by navArgs()
//        val receivedData = args.movie
//        val movieId = arguments?.getInt("movieId")
  //      val args = DetailFragmentArgs.fromBundle(requireArguments())
    }

}