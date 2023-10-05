package com.example.movie_project.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_project.databinding.UsersItemBinding
import com.example.movie_project.models.UsersModel

class UsersAdapter(
    private val userList: ArrayList<UsersModel>,
    private var clickListener: UserClickListener? = null
) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    fun setClickListener(clickListener: UserClickListener) {
        this.clickListener = clickListener
    }

    fun updateUsersList(newUsersList: List<UsersModel>) {
        userList.clear()
        userList.addAll(newUsersList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: UsersItemBinding = DataBindingUtil.inflate(
            inflater,
            com.example.movie_project.R.layout.users_item,
            parent,
            false
        )
        return UsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val users = userList[position]
        users?.let {
            holder.view.users.email = it.email
        }

        holder.view.cardView.setOnClickListener {
            clickListener?.onUserClicked(users)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }


    class UsersViewHolder(var view: UsersItemBinding) : RecyclerView.ViewHolder(view.root) {

    }
}