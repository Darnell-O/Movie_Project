package com.example.movie_project.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.movie_project.models.MessageModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

//class MessagingAdapter (
//    private val options: FirebaseRecyclerOptions<MessageModel>,
//    private val currentUserName: String?
//): FirebaseRecyclerAdapter<MessageModel,ViewHolder>(options){
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: MessageModel) {
//        TODO("Not yet implemented")
//    }
//
//    companion object {
//        const val TAG = "MessageAdapter"
//        const val VIEW_TYPE_TEXT = 1
//        const val VIEW_TYPE_IMAGE = 2
//    }
//
//}