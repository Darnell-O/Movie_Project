package com.example.movie_project.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.movie_project.R
import com.example.movie_project.models.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class MessageAdapter(val context: Context, val messageList: ArrayList<MessageModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_RECIEVE = 1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 1) {
            val view : View = LayoutInflater.from(context).inflate(R.layout.received_messages, parent, false)
           return ReceivedMessageHolder(view)
        } else {
            val view : View = LayoutInflater.from(context).inflate(R.layout.send_layout , parent, false)
            return SentMessageHolder(view)
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentMessageHolder::class.java) {

            val messageHolder = holder as SentMessageHolder
            holder.sentMessage.text = currentMessage.message
        } else {
            val messageHolder = holder as ReceivedMessageHolder
            holder.receivedMessage.text = currentMessage.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)) {
            ITEM_SENT
        } else {
            ITEM_RECIEVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage = itemView.findViewById<TextView>(R.id.sent_message)
    }

    class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage = itemView.findViewById<TextView>(R.id.received_message)
    }
}