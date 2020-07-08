package com.rasyidabdulhalim.aquaza.fragments


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentChange
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.activities.AddDepotActivity
import com.rasyidabdulhalim.aquaza.activities.ChatActivity
import com.rasyidabdulhalim.aquaza.activities.DepotActivity
import com.rasyidabdulhalim.aquaza.activities.DepotInfoActivity
import com.rasyidabdulhalim.aquaza.adapters.DepotsAdapter
import com.rasyidabdulhalim.aquaza.callbacks.DepotCallback
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseFragment
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Depot
import com.rasyidabdulhalim.aquaza.models.Order
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper
import com.rasyidabdulhalim.aquaza.utils.RecyclerFormatter
import com.rasyidabdulhalim.aquaza.utils.hideView
import com.rasyidabdulhalim.aquaza.utils.showView
import kotlinx.android.synthetic.main.depot_fragment.*
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.collections.set

class DepotFragment : BaseFragment(), DepotCallback {
    private lateinit var depotsAdapter: DepotsAdapter
    private lateinit var prefs: SharedPreferences
    private var localeID: Locale = Locale("in", "ID")
    private var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)
    var totalPriceOrder=0
    var totalOrderSummary=0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        prefs = PreferenceHelper.defaultPrefs(activity!!)
        return inflater.inflate(R.layout.depot_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)/*
        val myUri = Uri.parse(prefs[K.AVATAR])
        Glide.with(this *//* context *//*)
            .load(myUri)
            .into(userImageView)*/
        loadCars()
        loadOrderSummary()
    }

    private fun initViews(v: View) {
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.itemAnimator = DefaultItemAnimator()
        rv.addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        depotsAdapter = DepotsAdapter(activity!!, this)
        rv.adapter = depotsAdapter
        rv.showShimmerAdapter()
        refreshbutton.setOnClickListener {
            getFragmentManager()?.beginTransaction()?.detach(this)?.attach(this)?.commit()
            activity?.toast("Daftar Sudah Di refresh")
        }
    }

    private fun loadCars() {
        getFirestore().collection(K.DEPOTS)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Timber.e("Error fetching depots $firebaseFirestoreException")
                        noCars()
                    }
                    if (querySnapshot == null || querySnapshot.isEmpty) {
                        noCars()
                    } else {
                        hasCars()
                        for (docChange in querySnapshot.documentChanges) {
                            when(docChange.type) {
                                DocumentChange.Type.ADDED -> {
                                    val car = docChange.document.toObject(Depot::class.java)
                                    depotsAdapter.addCar(car)
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    val car = docChange.document.toObject(Depot::class.java)
                                    depotsAdapter.updateCar(car)
                                }

                                DocumentChange.Type.REMOVED -> {
                                    val car = docChange.document.toObject(Depot::class.java)
                                    depotsAdapter.removeCar(car)
                                }

                            }

                        }

                    }
                }

    }

    private fun loadOrderSummary(){
        getFirestore().collection(K.ORDERS)
            .whereEqualTo("buyerId",getUid())
            .whereEqualTo(K.STATUS,K.RECEIVED)
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
                                totalPriceOrder+=order.price!!.toInt()
                                totalOrderSummary +=order.quantity!!
                                priceTotalOrder.text= formatRupiah.format(totalPriceOrder.toDouble()).toString()
                                totalOrder.text=totalOrderSummary.toString()
                            }
                            DocumentChange.Type.REMOVED -> {
                                val order = docChange.document.toObject(Order::class.java)
                                totalPriceOrder-=order.price!!.toInt()
                                totalOrderSummary -=order.quantity!!
                                priceTotalOrder.text= formatRupiah.format(totalPriceOrder.toDouble()).toString()
                                totalOrder.text=totalOrderSummary.toString()
                            }
                        }
                    }
                }
            }
    }
    private fun hasCars() {
        rv?.hideShimmerAdapter()
        empty?.hideView()
        rv?.showView()
    }

    private fun noCars() {
        rv?.hideShimmerAdapter()
        rv?.hideView()
        empty?.showView()
    }

    override fun onClick(v: View, depot: Depot) {
        when(v.id) {
            R.id.action -> {
                if (depot.sellerId == getUid()) {
                    val i = Intent(activity, AddDepotActivity::class.java)
                    i.putExtra(K.DEPOT, depot)
                    startActivity(i)
                    AppUtils.animateFadein(activity!!)
                } else {
                    val i = Intent(activity, DepotActivity::class.java)
                    i.putExtra(K.DEPOT, depot)
                    startActivity(i)
                    AppUtils.animateFadein(activity!!)
                }
            }

            R.id.moreOptions -> {
                if (depot.sellerId == getUid()) {
                    sellerOptions(depot)
                } else {
                    buyerOptions(depot)
                }
            }
            R.id.image -> {
                val i = Intent(activity, DepotActivity::class.java)
                i.putExtra(K.DEPOT, depot)
                startActivity(i)
                AppUtils.animateFadein(activity!!)
            }
            R.id.contact -> {
                if (depot.sellerId == getUid()) {
                  val i = Intent(activity, DepotInfoActivity::class.java)
                  i.putExtra(K.DEPOT, depot)
                  activity!!.startActivity(i)
                    AppUtils.animateEnterLeft(activity!!)

                } else {
                    val i = Intent(activity, ChatActivity::class.java)
                    i.putExtra(K.MY_ID, getUid())
                    i.putExtra(K.OTHER_ID, depot.id)
                    i.putExtra(K.CHAT_NAME, depot.sellerName)
                    activity!!.startActivity(i)
                    AppUtils.animateFadein(activity!!)
                }
            }
        }
    }

    private fun sellerOptions(depot: Depot) {
        val items = arrayOf<CharSequence>("Delete")

        val builder = AlertDialog.Builder(activity!!)
        builder.setItems(items) { _, item ->
            when(item) {
                0 -> {
                    getFirestore().collection(K.DEPOTS).document(depot.id!!).delete()
                        .addOnSuccessListener {
                            activity?.toast("Depot ${depot.depotName} deleted!")
                        }
                        .addOnFailureListener {
                            Timber.e("Error deleting ${depot.id}")
                            activity?.toast("Error deleting ${depot.depotName}")
                        }
                }
            }
        }
        val alert = builder.create()
        alert.show()
    }

    private fun buyerOptions(depot: Depot) {
        val builder = AlertDialog.Builder(activity!!)

        if (depot.watchlist.containsKey(getUid())) {
            val items = arrayOf<CharSequence>("Remove from watchlist")

            builder.setItems(items) { _, item ->
                when(item) {
                    0 -> {
                        removeFromWatchlist(depot)
                    }
                }
            }

        } else {
            val items = arrayOf<CharSequence>("Add to watchlist")

            builder.setItems(items) { _, item ->
                when(item) {
                    0 -> {
                        addToWatchList(depot)
                    }
                }
            }

        }

        val alert = builder.create()
        alert.show()
    }

    private fun addToWatchList(depot: Depot) {
        val docRef = getFirestore().collection(K.DEPOTS).document(depot.id!!)

        getFirestore().runTransaction {
            val snapshot = it[docRef].toObject(Depot::class.java)
            val watchlist = snapshot?.watchlist
            watchlist?.put(getUid(), true)
            depot.watchlist[getUid()] = true

            it.update(docRef, K.WATCHLIST, watchlist)

            return@runTransaction null
        }.addOnSuccessListener {
            Timber.e("Successfully added ${depot.id} to ${getUid()} watchlist")
            activity?.toast("${depot.depotName} added to watchlist")

            getFirestore().collection(K.WATCHLIST).document(getUid()).collection(K.DEPOTS).document(depot.id!!).set(depot)
        }.addOnFailureListener {
            Timber.e("Error adding ${depot.id} to watchlist: $it")
        }
    }

    private fun removeFromWatchlist(depot: Depot) {
        val docRef = getFirestore().collection(K.DEPOTS).document(depot.id!!)

        getFirestore().runTransaction {
            val snapshot = it[docRef].toObject(Depot::class.java)
            val watchlist = snapshot?.watchlist
            watchlist?.remove(getUid())
            depot.watchlist.remove(getUid())

            it.update(docRef, K.WATCHLIST, watchlist)

            return@runTransaction null
        }.addOnSuccessListener {
            Timber.e("Successfully removed ${depot.id} from ${getUid()} watchlist")
            activity?.toast("${depot.depotName} removed from watchlist")

            getFirestore().collection(K.WATCHLIST).document(getUid()).collection(K.DEPOTS).document(depot.id!!).delete()
        }.addOnFailureListener {
            Timber.e("Error removing ${depot.id} from watchlist: $it")
        }
    }
}
