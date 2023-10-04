package com.example.movie_project.views

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movie_project.Login_Activity
import com.example.movie_project.R
import com.example.movie_project.databinding.ActivityMessagingBinding
import com.example.movie_project.databinding.MessageBinding
import com.example.movie_project.models.MessageModel
import com.example.movie_project.util.OpenDocumentContract
import com.example.movie_project.util.ScrollToBottomObserver
import com.example.movie_project.util.SendButtonObserver
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class MessagingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessagingBinding
    private lateinit var manager: LinearLayoutManager

    private lateinit var messagingAuth: FirebaseAuth
    private lateinit var messagingDatabase: FirebaseDatabase
    private lateinit var messagingAdapter: MessagingAdapter

    private val openDocument = registerForActivityResult(OpenDocumentContract()) { uri ->
        uri?.let { onImageSelected(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signOut()
        messagingAuth = Firebase.auth
        messagingDatabase = Firebase.database
        val messagesRef = messagingDatabase.reference.child(MESSAGES_CHILD)

        val options = FirebaseRecyclerOptions.Builder<MessageModel>()
            .setQuery(messagesRef, MessageModel::class.java)
            .build()
        messagingAdapter = MessagingAdapter(options, getUserName())
        manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        binding.toolbarUsername.text = getUserName()
        binding.messageRecyclerView.layoutManager = manager
        binding.messageRecyclerView.adapter = messagingAdapter

        messagingAdapter.registerAdapterDataObserver(
            ScrollToBottomObserver(binding.messageRecyclerView, messagingAdapter, manager)
        )
        binding.messageEditText.addTextChangedListener(SendButtonObserver(binding.sendButton))

        binding.sendButton.setOnClickListener {
            val message = MessageModel(
                binding.messageEditText.text.toString(),
                messagingAuth.currentUser?.displayName,
                messagingAuth.currentUser?.uid
            )
            messagingDatabase.reference.child(MESSAGES_CHILD).push().setValue(message)
            binding.messageEditText.setText("")
        }
        binding.addMessageImageView.setOnClickListener {
            openDocument.launch(arrayOf("image/*"))
        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in.
        if (messagingAuth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, Login_Activity::class.java))
            finish()
            return
        }
    }

    public override fun onPause() {
        messagingAdapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        messagingAdapter.startListening()
    }

    private fun onImageSelected(uri: Uri) {
        Log.d(TAG, "Uri: $uri")
        val user = messagingAuth.currentUser
        val tempMessage = MessageModel(null, getUserName(), getPhotoUrl(), LOADING_IMAGE_URL)
        messagingDatabase.reference
            .child(MESSAGES_CHILD)
            .push()
            .setValue(
                tempMessage,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Log.w(
                            TAG, "Unable to write message to database.",
                            databaseError.toException()
                        )
                        return@CompletionListener
                    }

                    // Build a StorageReference and then upload the file
                    val key = databaseReference.key
                    val storageReference = Firebase.storage
                        .getReference(user!!.uid)
                        .child(key!!)
                        .child(uri.lastPathSegment!!)
                    putImageInStorage(storageReference, uri, key)
                })
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String?) {
        // First upload the image to Cloud Storage
        storageReference.putFile(uri)
            .addOnSuccessListener(
                this
            ) { taskSnapshot -> // After the image loads, get a public downloadUrl for the image
                // and add it to the message.
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        val friendlyMessage =
                            MessageModel(null, getUserName(), getPhotoUrl(), uri.toString())
                        messagingDatabase.reference
                            .child(MESSAGES_CHILD)
                            .child(key!!)
                            .setValue(friendlyMessage)
                    }
            }
            .addOnFailureListener(this) { e ->
                Log.w(
                    TAG,
                    "Image upload task was unsuccessful.",
                    e
                )
            }
    }

    private fun signOut() {
        binding.toolbarProfileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun getPhotoUrl(): String? {
        val user = messagingAuth.currentUser
        return user?.photoUrl?.toString()
    }

    private fun getUserName(): String? {
        val user = messagingAuth.currentUser
        return if (user != null) {
            user.displayName
        } else ANONYMOUS
    }


    companion object {
        private const val TAG = "MessagingFragment"
        const val MESSAGES_CHILD = "messages"
        const val ANONYMOUS = "anonymous"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}