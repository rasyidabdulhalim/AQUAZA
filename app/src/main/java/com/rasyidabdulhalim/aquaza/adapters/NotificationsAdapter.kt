package com.rasyidabdulhalim.aquaza.adapters

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.databinding.ItemNotifBinding
import com.rasyidabdulhalim.aquaza.models.Notification
import com.rasyidabdulhalim.aquaza.utils.TimeFormatter
import com.rasyidabdulhalim.aquaza.utils.inflate

class NotificationsAdapter : RecyclerView.Adapter<NotificationsAdapter.NotificationHolder>() {
    private val notifs = mutableListOf<Notification>()

    fun addNotif(notification: Notification) {
        notifs.add(notification)
        notifyItemInserted(notifs.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        return NotificationHolder(parent.inflate(R.layout.item_notif))
    }

    override fun getItemCount(): Int = notifs.size

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.bind(notifs[position])
    }

    class NotificationHolder(private val binding: ItemNotifBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.notif = notification
            binding.time = TimeFormatter().getTimeStamp(notification.time!!)
        }

    }

}