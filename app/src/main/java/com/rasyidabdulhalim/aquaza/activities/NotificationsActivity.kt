package com.rasyidabdulhalim.aquaza.activities


import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.google.firebase.firestore.DocumentChange
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.adapters.NotificationsAdapter
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Notification
import com.rasyidabdulhalim.aquaza.utils.RecyclerFormatter
import kotlinx.android.synthetic.main.activity_notification.*
import timber.log.Timber


class NotificationsActivity : BaseActivity() {
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        initViews()
    }
    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Notifikasi"

        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)
        rv.itemAnimator = DefaultItemAnimator()
        rv.addItemDecoration(RecyclerFormatter.SimpleDividerItemDecoration(this))

        notificationsAdapter = NotificationsAdapter()
        rv.adapter = notificationsAdapter
        rv.showShimmerAdapter()

        Handler().postDelayed({
            rv.hideShimmerAdapter()
            loadSample()
        }, 650)
    }

    private fun loadSample() {
        getFirestore().collection(K.NOTIFICATIONS)
            .orderBy("time",com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {

                } else {
                    for (docChange in querySnapshot.documentChanges) {
                        when(docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val notification = docChange.document.toObject(Notification::class.java)
                                if (notification.uid==getUid()){
                                    notificationsAdapter.addNotif(notification)
                                }
                            }

                        }
                    }

                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.animateEnterLeft(this)
    }

}
