package com.rasyidabdulhalim.aquaza.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.activities.ChatActivity
import com.rasyidabdulhalim.aquaza.adapters.ChatsAdapter
import com.rasyidabdulhalim.aquaza.callbacks.ChatListCallback
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseFragment
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Chat
import com.rasyidabdulhalim.aquaza.utils.hideView
import com.rasyidabdulhalim.aquaza.utils.showView
import kotlinx.android.synthetic.main.chat_fragment.*
import timber.log.Timber


class ChatFragment : BaseFragment(), ChatListCallback {
    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var chatsQuery: Query

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        chatsQuery = getDatabaseReference().child(K.CHATS).child(getUid())
        chatsQuery.addValueEventListener(chatsValueListener)
        chatsQuery.addChildEventListener(chatsChildListener)

        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
    }

    private fun initRecycler() {
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(activity!!)
        rv.itemAnimator = DefaultItemAnimator()

        chatsAdapter = ChatsAdapter(this)
        rv.adapter = chatsAdapter
        rv.showShimmerAdapter()
    }

    private val chatsValueListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Timber.e("Error fetching chats: $p0")
            noChats()
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                hasChats()
            } else {
                noChats()
            }
        }
    }

    private val chatsChildListener = object : ChildEventListener {

        override fun onCancelled(p0: DatabaseError) {
            Timber.e("Child listener cancelled: $p0")
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            Timber.e("Chat moved: ${p0.key}")
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            val chat = p0.getValue(Chat::class.java)
            chatsAdapter.updateChat(chat!!)
        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val chat = p0.getValue(Chat::class.java)
            chatsAdapter.addChat(chat!!)
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            Timber.e("Chat removed: ${p0.key}")
        }
    }

    private fun hasChats() {
        rv?.hideShimmerAdapter()
        empty?.hideView()
        rv?.showView()
    }

    private fun noChats() {
        rv?.hideShimmerAdapter()
        rv?.hideView()
        empty?.showView()
    }

    override fun onClick(chat: Chat) {
        val i = Intent(activity, ChatActivity::class.java)
        i.putExtra(K.MY_ID, getUid())
        i.putExtra(K.OTHER_ID, AppUtils.getID2(chat.id!!, getUid()))
        i.putExtra(K.CHAT_NAME, chat.username)
        activity!!.startActivity(i)
        AppUtils.animateFadein(activity!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        chatsQuery.removeEventListener(chatsValueListener)
        chatsQuery.removeEventListener(chatsChildListener)
    }
}
