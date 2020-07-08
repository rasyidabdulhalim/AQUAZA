package com.rasyidabdulhalim.aquaza.fragments


import android.content.*
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentChange
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.activities.ChatActivity
import com.rasyidabdulhalim.aquaza.adapters.HistoryOrdersAdapter
import com.rasyidabdulhalim.aquaza.callbacks.OrderCallBack
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseFragment
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Order
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper.get
import com.rasyidabdulhalim.aquaza.utils.RecyclerFormatter
import com.rasyidabdulhalim.aquaza.utils.hideView
import com.rasyidabdulhalim.aquaza.utils.showView
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.history_order_item.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber

class OrderHistoryFragment : BaseFragment(), OrderCallBack {
    private lateinit var orderAdapter: HistoryOrdersAdapter
    private lateinit var prefs: SharedPreferences
    private var noOrder: Boolean = true
    private var isOpen: Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        prefs = PreferenceHelper.defaultPrefs(activity!!)
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        if (prefs[K.STATUS, ""] == "Owner") {
            loadOrdersOwner()
        } else if (prefs[K.STATUS, ""] == "Driver") {
            loadOrdersDriver()
        } else {
            loadOrdersUser()
        }
    }

    private fun initViews(v: View) {
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.itemAnimator = DefaultItemAnimator()
        rv.addItemDecoration(RecyclerFormatter.SimpleDividerItemDecoration(activity!!))
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        orderAdapter = HistoryOrdersAdapter(this)
        rv.adapter = orderAdapter
        rv.showShimmerAdapter()
    }

    /*override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            getFragmentManager()?.beginTransaction()?.detach(this)?.attach(this)?.commit();//refresh
        }
    }*/

    private fun loadOrdersUser() {
        getFirestore().collection(K.ORDERS)
            .whereEqualTo("buyerId",getUid())
            //.whereEqualTo("status", "Received")
            .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")
                    noOrders()
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {
                    noOrders()
                } else {
                    hasOrders()
                    for (docChange in querySnapshot.documentChanges) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.addItem(order)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.updateItem(order)
                            }

                            DocumentChange.Type.REMOVED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.removeItem(order)
                            }

                        }

                    }

                }
            }
        if (noOrder) noOrders()
    }

    private fun loadOrdersOwner() {
        getFirestore().collection(K.ORDERS)
            .whereEqualTo("status", "Received")
            .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {

                } else {
                    hasOrders()
                    for (docChange in querySnapshot.documentChanges) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.addItem(order)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.updateItem(order)
                            }

                            DocumentChange.Type.REMOVED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.removeItem(order)
                            }

                        }

                    }

                }
            }
        if (noOrder) noOrders()
    }

    private fun loadOrdersDriver() {
        getFirestore().collection(K.ORDERS)
            .whereEqualTo("buyerId", getUid())
            .whereEqualTo("status", K.ONPROSES)
            .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")

                }
                if (querySnapshot == null || querySnapshot.isEmpty) {

                } else {
                    hasOrders()
                    for (docChange in querySnapshot.documentChanges) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.addItem(order)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.updateItem(order)
                            }

                            DocumentChange.Type.REMOVED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.removeItem(order)
                            }

                        }

                    }

                }
            }

    }

    private fun hasOrders() {
        noOrder = true
        rv?.hideShimmerAdapter()
        empty?.hideView()
        rv?.showView()
    }

    private fun noOrders() {
        rv?.hideShimmerAdapter()
        rv?.hideView()
        empty?.showView()
    }

    override fun onClick(v: View, order: Order) {
        when (v.id) {
            R.id.action -> {
                if (order.buyerId == getUid()) {
                } else {
                    activity?.alert("Pesanan Sudah Sampai Ketujuan?") {
                        positiveButton("YES") {
                            getFirestore().collection(K.ORDERS).document(order.id!!)
                                .update("status", "Received").addOnSuccessListener {
                                orderAdapter.updateItem(order)
                                activity?.toast("Terimakasih,Hati-Hati dalam Perjalanan~")
                            }
                        }
                        negativeButton("CANCEL") {}
                    }!!.show()
                }
            }
            R.id.contact -> {
                if (order.buyerId == getUid()) {
                    val i = Intent(activity, ChatActivity::class.java)
                    i.putExtra(K.MY_ID, getUid())
                    i.putExtra(K.OTHER_ID, order.depotId)
                    i.putExtra(K.CHAT_NAME, order.depotName)
                    activity!!.startActivity(i)
                    AppUtils.animateFadein(activity!!)
                } else {
                    val i = Intent(activity, ChatActivity::class.java)
                    i.putExtra(K.MY_ID, getUid())
                    i.putExtra(K.OTHER_ID, order.buyerId)
                    i.putExtra(K.CHAT_NAME, order.buyerName)
                    activity!!.startActivity(i)
                    AppUtils.animateFadein(activity!!)
                }
            }
            R.id.cancel -> {
                if (order.buyerId == getUid()) {
                    activity?.alert("Batalkan Pesanan?") {
                        positiveButton("YES") {
                            getFirestore().collection(K.ORDERS).document(order.id!!).delete()
                                .addOnSuccessListener {
                                    activity?.toast("Pesanan Berhasil Dibatalkan")
                                    orderAdapter.removeItem(order)
                                }
                                .addOnFailureListener {
                                    Timber.e("Error deleting ${order.id}")
                                    activity?.toast("Error deleting ${order.id}")
                                }
                        }
                        negativeButton("CANCEL") {}
                    }!!.show()
                } else {
                    activity?.alert("Ambil Pesanan?") {
                        positiveButton("YES") {
                            getFirestore().collection(K.ORDERS).document(order.id!!)
                                .update("status", "On Progress").addOnSuccessListener {
                                orderAdapter.updateItem(order)
                                activity?.toast("Pesanan Berhasil Diambil,Silahkan Antarkan Pesanan~")
                            }
                        }
                        negativeButton("CANCEL") {}
                    }!!.show()
                }
            }
            R.id.placeDetailOrder -> {

            }
            R.id.copyImageView1 -> {
                copyImageView1.setOnClickListener {
                    val cManager =
                        activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val cData = ClipData.newPlainText("text", transactionNoTextView1.text)
                    cManager.primaryClip = cData
                    activity?.toast("Copied to clipboard")
                }
            }
        }
    }

}
