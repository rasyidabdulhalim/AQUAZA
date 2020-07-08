package com.rasyidabdulhalim.aquaza.fragments


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentChange
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.activities.ChatActivity
import com.rasyidabdulhalim.aquaza.activities.MapActivity
import com.rasyidabdulhalim.aquaza.adapters.OrdersAdapter
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
import kotlinx.android.synthetic.main.fragment_order.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber

class OrderFragment : BaseFragment(),OrderCallBack{
    private lateinit var orderAdapter: OrdersAdapter
    private lateinit var prefs: SharedPreferences
    private  var noOrder:Boolean = true
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        prefs = PreferenceHelper.defaultPrefs(activity!!)
        return inflater.inflate(R.layout.fragment_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        if(prefs[K.STATUS, ""]=="Owner"){
            loadOrdersOwner()
        }else if (prefs[K.STATUS, ""]=="Driver"){
            loadOrdersDriver()
        }else{
            loadOrdersUser()
        }
    }
    private fun initViews(v: View) {
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.itemAnimator = DefaultItemAnimator()
        rv.addItemDecoration(RecyclerFormatter.SimpleDividerItemDecoration(activity!!))
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        orderAdapter = OrdersAdapter(activity!!,this)
        rv.adapter = orderAdapter
        rv.showShimmerAdapter()
    }

    private fun loadOrdersUser() {
        getFirestore().collection(K.ORDERS)
            .whereEqualTo("buyerId",getUid())
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
                                val order = docChange.document.toObject(Order::class.java)
                                if(order.status!="Received") {
                                    orderAdapter.addItem(order)
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                if(order.status!="Received") {
                                    orderAdapter.updateItem(order)
                                }else{
                                    orderAdapter.removeItem(order)

                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.removeItem(order)
                            }

                        }
                    }
                    checkOrder()
                }
            }
        checkOrder()
    }
    private fun loadOrdersOwner() {
        getFirestore().collection(K.ORDERS)
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
                                val order = docChange.document.toObject(Order::class.java)
                                if(order.status!="Received") {
                                    orderAdapter.addItem(order)
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                if(order.status!="Received") {
                                    orderAdapter.updateItem(order)
                                }else{
                                    orderAdapter.removeItem(order)

                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.removeItem(order)
                            }

                        }
                    }
                    checkOrder()
                }
            }
        checkOrder()
    }
    private fun loadOrdersDriver() {
        getFirestore().collection(K.ORDERS)
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
                                val order = docChange.document.toObject(Order::class.java)
                                if((order.status!="Received"&&order.driverId==null)||(order.status!="Received"&&order.driverId==getUid())) {
                                    orderAdapter.addItem(order)
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                if((order.status!="Received"&&order.driverId==null)||(order.status!="Received"&&order.driverId==getUid())) {
                                    orderAdapter.updateItem(order)
                                }else if(order.status==K.RECEIVED&&order.driverId==getUid()){
                                    orderAdapter.removeItem(order)
                                    orderAdapter.notifyDataSetChanged()
                                }else if(order.status==K.ONPROSES&&order.driverId!=getUid()){
                                    orderAdapter.removeItem(order)
                                    orderAdapter.notifyDataSetChanged()
                                }else{

                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                orderAdapter.removeItem(order)
                            }

                        }
                    }
                    checkOrder()
                }
            }
        checkOrder()
    }
    private fun checkOrder(){
        if(orderAdapter.itemCount>0){
            hasOrders()
        }else{
            noOrders()
        }
    }
    private fun hasOrders() {
        noOrder=true
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
        when(v.id) {
            R.id.action->{
                if(order.buyerId==getUid()){
                    val i = Intent(activity, MapActivity::class.java)
                    i.putExtra(K.ORDER, order)
                    activity!!.startActivity(i)
                    AppUtils.animateEnterLeft(activity!!)
                }else{
                    activity?.alert("Pesanan Sudah Sampai Ketujuan?") {
                        positiveButton("YES") {
                            order.status=K.RECEIVED
                            getFirestore().collection(K.ORDERS).document(order.id!!).set(order).addOnSuccessListener {
                                activity?.toast("Terimakasih,Hati-Hati dalam Perjalanan~")
                            }
                        }
                        negativeButton("CANCEL") {}
                    }!!.show()
                }
            }
            R.id.contact->{
                if(order.buyerId==getUid()){
                    val i = Intent(activity, ChatActivity::class.java)
                    i.putExtra(K.MY_ID, getUid())
                    i.putExtra(K.OTHER_ID, order.depotId)
                    i.putExtra(K.CHAT_NAME, order.depotName)
                    activity!!.startActivity(i)
                    AppUtils.animateFadein(activity!!)
                }else{
                    val i = Intent(activity, ChatActivity::class.java)
                    i.putExtra(K.MY_ID, getUid())
                    i.putExtra(K.OTHER_ID, order.buyerId)
                    i.putExtra(K.CHAT_NAME, order.buyerName)
                    activity!!.startActivity(i)
                    AppUtils.animateFadein(activity!!)
                }
            }
            R.id.cancel->{
                if(order.buyerId==getUid()){
                    activity?.alert("Batalkan Pesanan?") {
                        positiveButton("YES") {
                            if(orderAdapter.itemCount==1){
                                orderAdapter.clear()
                                noOrders()
                            }
                            getFirestore().collection(K.ORDERS).document(order.id!!).delete()
                                .addOnSuccessListener {
                                    activity?.toast("Pesanan Berhasil Dibatalkan")
                                }
                                .addOnFailureListener {
                                    Timber.e("Error deleting ${order.id}")
                                    activity?.toast("Error deleting ${order.id}")
                                }
                        }
                        negativeButton("CANCEL") {}
                    }!!.show()
                }else{
                    activity?.alert("Ambil Pesanan?") {
                        positiveButton("YES") {
                            order.status=K.ONPROSES
                            order.driverId=getUid()
                            order.driverName=prefs[K.NAME]
                            getFirestore().collection(K.ORDERS).document(order.id!!).set(order).addOnSuccessListener {
                                activity?.toast("Pesanan Berhasil Diambil,Silahkan Antarkan Pesanan~")
                            }
                        }
                        negativeButton("CANCEL") {}
                    }!!.show()
                }
            }
            R.id.placeDetailOrder->{
                val i = Intent(activity, MapActivity::class.java)
                i.putExtra(K.ORDER, order)
                activity!!.startActivity(i)
                AppUtils.animateEnterLeft(activity!!)
            }
        }
    }
}
