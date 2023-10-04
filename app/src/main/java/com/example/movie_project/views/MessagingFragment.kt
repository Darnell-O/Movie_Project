package com.example.movie_project.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movie_project.R
import com.example.movie_project.databinding.FragmentMessagingBinding
import com.example.movie_project.models.MessageModel
import com.example.movie_project.util.OpenDocumentContract
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [MessagingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagingFragment : Fragment() {

    private lateinit var binding: FragmentMessagingBinding
    private lateinit var manager: LinearLayoutManager
    // Firebase instance variables
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: MessagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val intent = Intent(activity, MessagingActivity::class.java)
        startActivity(intent)

        // Inflate the layout for this fragment
//        val binding = FragmentMessagingBinding.inflate(inflater, container, false)
//        db = Firebase.database
//        val messagesRef = db.reference.child(MESSAGES_CHILD)
//        val options = FirebaseRecyclerOptions.Builder<MessageModel>()
//            .setQuery(messagesRef, MessageModel::class.java)
//            .build()



//        auth = Firebase.auth
//        if (auth.currentUser == null) {
//            // Not signed in, launch the Sign In activity
//            startActivity(Intent(this, SignInActivity::class.java))
//            finish()
//            return
//        }


        return binding.root
    }

    companion object {
        private const val TAG = "MessagingFragment"
        const val MESSAGES_CHILD = "messages"
        const val ANONYMOUS = "anonymous"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}