package com.rasyidabdulhalim.aquaza.adapters

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.databinding.ChatItemMessageBinding
import com.rasyidabdulhalim.aquaza.databinding.ChatItemMessageMeBinding
import com.rasyidabdulhalim.aquaza.models.Message
import com.rasyidabdulhalim.aquaza.utils.inflate

class MessagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messages = mutableListOf<Message>()

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun lastPosition(): Int = messages.size - 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == K.ME) {
            ChatMeHolder(parent.inflate(R.layout.chat_item_message_me))
        } else {
            ChatHolder(parent.inflate(R.layout.chat_item_message))
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == FirebaseAuth.getInstance().currentUser!!.uid) {
            K.ME
        } else {
            K.OTHER
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChatHolder) {
            holder.bind(messages[position])
        } else if (holder is ChatMeHolder) {
            holder.bind(messages[position])
        }

    }

    class ChatMeHolder(private val binding: ChatItemMessageMeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.message = message
        }

    }

    class ChatHolder(private val binding: ChatItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.message = message
        }

    }
}