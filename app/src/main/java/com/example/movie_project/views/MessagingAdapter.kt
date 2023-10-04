package com.example.movie_project.views

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.movie_project.R
import com.example.movie_project.databinding.ImageMessageBinding
import com.example.movie_project.databinding.MessageBinding
import com.example.movie_project.models.MessageModel
import com.example.movie_project.views.MessagingFragment.Companion.ANONYMOUS
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MessagingAdapter(
    private val options: FirebaseRecyclerOptions<MessageModel>,
    private val currentUserName: String?
) : FirebaseRecyclerAdapter<MessageModel, ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_TEXT) {
            val view = inflater.inflate(R.layout.message, parent, false)
            val binding = MessageBinding.bind(view)
            MessageViewHolder(binding)
        } else {
            val view = inflater.inflate(R.layout.image_message, parent, false)
            val binding = ImageMessageBinding.bind(view)
            ImageMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: MessageModel) {
        if (options.snapshots[position].text != null) {
            (holder as MessageViewHolder).bind(model)
        } else {
            (holder as ImageMessageViewHolder).bind(model)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (options.snapshots[position].text != null) VIEW_TYPE_TEXT else VIEW_TYPE_IMAGE
    }

    inner class MessageViewHolder(private val binding: MessageBinding) : ViewHolder(binding.root) {
        fun bind(item: MessageModel) {
            binding.messageTextView.text = item.text
            setTextColor(item.name, binding.messageTextView)

            binding.messengerTextView.text = item.name ?: ANONYMOUS
            if (item.photoUrl != null) {
                loadImageIntoView(binding.messengerImageView, item.photoUrl)
//                Glide.with(binding.messengerImageView.context)
//                    .load(item.photoUrl)
//                    .into(binding.messengerImageView)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.outline_account_circle_24)
            }
        }

        private fun setTextColor(userName: String?, textView: TextView) {
            if (userName != ANONYMOUS && currentUserName == userName && userName != null) {
                textView.setBackgroundResource(R.drawable.rounded_message_blue)
                textView.setTextColor(Color.WHITE)
            } else {
                textView.setBackgroundResource(R.drawable.rounded_message_gray)
                textView.setTextColor(Color.BLACK)
            }
        }
    }

    inner class ImageMessageViewHolder(private val binding: ImageMessageBinding) :
        ViewHolder(binding.root) {
        fun bind(item: MessageModel) {
            loadImageIntoView(binding.messageImageView, item.imageUrl!!, false)

            binding.messengerTextView.text = item.name ?: ANONYMOUS
            if (item.photoUrl != null) {
                loadImageIntoView(binding.messengerImageView, item.photoUrl)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.outline_account_circle_24)
            }
        }
    }

    private fun loadImageIntoView(view: ImageView, url: String, isCircular: Boolean = true) {
        if (url.startsWith("gs://")) {
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    loadWithGlide(view, downloadUrl, isCircular)
                }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG,
                        "Getting download url was not successful.",
                        e
                    )
                }
        } else {
            loadWithGlide(view, url, isCircular)
        }
    }

    private fun loadWithGlide(view: ImageView, url: String, isCircular: Boolean = true) {
        Glide.with(view.context).load(url).into(view)
        var requestBuilder = Glide.with(view.context).load(url)
        if (isCircular) {
            requestBuilder = requestBuilder.transform(CircleCrop())
        }
        requestBuilder.into(view)
    }

    companion object {
        const val TAG = "MessageAdapter"
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
    }

}