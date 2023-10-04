package com.example.movie_project.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import com.example.movie_project.R

class SendButtonObserver(private val sendButton: ImageView) : TextWatcher{

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.toString().trim().isNotEmpty()) {
            sendButton.isEnabled = true
            sendButton.setImageResource(R.drawable.round_send_send24)
        } else {
            sendButton.isEnabled = false
            sendButton.setImageResource(R.drawable.round_send_gray24)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable?) {}
}