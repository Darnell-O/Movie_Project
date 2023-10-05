package com.example.movie_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_project.databinding.ActivityUsersBinding
import com.example.movie_project.models.UsersModel
import com.example.movie_project.views.MessagesActivity
import com.example.movie_project.views.UserClickListener
import com.example.movie_project.views.UsersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersActivity : AppCompatActivity(), UserClickListener {
    private lateinit var binding: ActivityUsersBinding
    private lateinit var usersList: ArrayList<UsersModel>
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var usersDatabase: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        usersDatabase = FirebaseDatabase.getInstance().getReference()
        userRecyclerView = findViewById(R.id.users_recyclerView)

        usersList = ArrayList()
        usersAdapter = UsersAdapter(usersList, this)
        usersAdapter.setClickListener(this)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.usersRecyclerView.adapter = usersAdapter

        usersDatabase.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(UsersModel::class.java)
                    if (firebaseAuth.currentUser?.uid != user?.uid) {
                        user?.let { usersList.add(it) }
                    }
                    usersList.add(user!!)
                }
                usersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UsersActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })


    }

    override fun onUserClicked(user: UsersModel) {
        Toast.makeText(this, "${user?.email}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@UsersActivity, MessagesActivity::class.java)
        val bundle = Bundle()
        user.email?.let { bundle.putString("email", firebaseAuth.currentUser?.email) }
        user.uid?.let { bundle.putString("uid", firebaseAuth.currentUser?.uid) }
        intent.putExtras(bundle)
        startActivity(intent)
    }
}