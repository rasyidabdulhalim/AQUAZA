package com.rasyidabdulhalim.aquaza.fragments


import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentChange

import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.adapters.NotificationsAdapter
import com.rasyidabdulhalim.aquaza.commoners.BaseFragment
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Notification
import com.rasyidabdulhalim.aquaza.utils.RecyclerFormatter
import kotlinx.android.synthetic.main.fragment_my_board_employee.view.*
import timber.log.Timber


class MyEmployeeBoardFragment : BaseFragment() {
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_board_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    private fun initViews(v: View) {
        v.rv.setHasFixedSize(true)
        v.rv.layoutManager = LinearLayoutManager(activity!!)
        v.rv.itemAnimator = DefaultItemAnimator()
        v.rv.addItemDecoration(RecyclerFormatter.SimpleDividerItemDecoration(activity!!))

        notificationsAdapter = NotificationsAdapter()
        v.rv.adapter = notificationsAdapter
        v.rv.showShimmerAdapter()

        Handler().postDelayed({
            v.rv.hideShimmerAdapter()
            loadSample()
        }, 1500)
    }

    private fun loadSample() {
        getFirestore().collection(K.NOTIFICATIONS)
            .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {

                } else {
                    for (docChange in querySnapshot.documentChanges) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val notification =
                                    docChange.document.toObject(Notification::class.java)
                                notificationsAdapter.addNotif(notification)
                            }

                        }
                    }

                }
            }

    }

}
