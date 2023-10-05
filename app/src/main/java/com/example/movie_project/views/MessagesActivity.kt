package com.example.movie_project.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_project.R
import com.example.movie_project.models.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessagesActivity : AppCompatActivity() {
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<MessageModel>
    private lateinit var databaseReference: DatabaseReference

    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        val email = intent.getStringExtra("email")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = email

        messageRecyclerView = findViewById(R.id.messages_recycler_view)
        messageBox = findViewById(R.id.message_input_layout)
        sendButton = findViewById(R.id.send_button)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)

        databaseReference.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val message = dataSnapshot.getValue(MessageModel::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MessagesActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })

        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = MessageModel(senderUid!!, message)
            databaseReference.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    databaseReference.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            messageBox.setText("")

        }

    }
}