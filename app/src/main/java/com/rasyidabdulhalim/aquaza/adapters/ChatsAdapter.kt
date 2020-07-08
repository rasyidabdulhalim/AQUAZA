package com.rasyidabdulhalim.aquaza.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.callbacks.ChatListCallback
import com.rasyidabdulhalim.aquaza.databinding.ChatItemBinding
import com.rasyidabdulhalim.aquaza.models.Chat
import com.rasyidabdulhalim.aquaza.utils.TimeFormatter
import com.rasyidabdulhalim.aquaza.utils.inflate

class ChatsAdapter(private val callback: ChatListCallback) : RecyclerView.Adapter<ChatsAdapter.ChatListHolder>() {
    private var chats = mutableListOf<Chat>()

    fun addChat(chat: Chat) {
        chats.add(chat)
        notifyItemInserted(chats.size - 1)
    }

    fun updateChat(updatedChat: Chat) {
        for ((index, chat) in chats.withIndex()) {
            if (updatedChat.id == chat.id) {
                chats[index] = updatedChat
                notifyItemChanged(index, updatedChat)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListHolder {
        return ChatListHolder(parent.inflate(R.layout.chat_item), callback)
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: ChatListHolder, position: Int) {
        holder.bindViews(chats[position])
    }

    interface OnItemClickListener {
        fun onItemClickListener(chat: Chat)
    }

    class ChatListHolder(private val binding: ChatItemBinding, callback: ChatListCallback) : RecyclerView.ViewHolder(binding.root){

        init {
            binding.callback = callback
        }

        fun bindViews(chat: Chat) {
            binding.data = chat
            binding.time = TimeFormatter().getChatTimeStamp(chat.time!!)
        }

    }

}