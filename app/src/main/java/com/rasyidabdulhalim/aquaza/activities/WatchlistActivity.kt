package com.rasyidabdulhalim.aquaza.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.adapters.DepotsAdapter
import com.rasyidabdulhalim.aquaza.callbacks.DepotCallback
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Depot
import com.rasyidabdulhalim.aquaza.utils.RecyclerFormatter
import com.rasyidabdulhalim.aquaza.utils.hideView
import com.rasyidabdulhalim.aquaza.utils.showView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_watchlist.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber

class WatchlistActivity : BaseActivity(), DepotCallback {
    private lateinit var depotsAdapter: DepotsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)

        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Depot Langganan"

        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)
        rv.itemAnimator = DefaultItemAnimator()
        rv.addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(this))

        depotsAdapter = DepotsAdapter(this, this)
        rv.adapter = depotsAdapter
        rv.showShimmerAdapter()

        loadCars()
    }

    private fun loadCars() {
        getFirestore().collection(K.WATCHLIST).document(getUid()).collection(K.DEPOTS)
                .orderBy(K.TIMESTAMP, Query.Direction.DESCENDING)
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
                    alert("Mark ${depot.depotName} sold?") {
                        positiveButton("YES") {}
                        negativeButton("CANCEL") {}
                    }.show()

                } else {
                    val i = Intent(this, DepotActivity::class.java)
                    i.putExtra(K.DEPOT, depot)
                    startActivity(i)
                    AppUtils.animateFadein(this)
                }
            }

            R.id.moreOptions -> {
                if (depot.sellerId == getUid()) {
                    //sellerOptions(car)
                } else {
                    buyerOptions(depot)
                }
            }

            R.id.image -> {
                val i = Intent(this, DepotActivity::class.java)
                i.putExtra(K.DEPOT, depot)
                startActivity(i)
                AppUtils.animateFadein(this)
            }

            R.id.contact -> {
                if (depot.sellerId == getUid()) {
                 /*   val i = Intent(this, AddCarActivity::class.java)
                    startActivity(i)
                    AppUtils.animateEnterLeft(this)*/

                } else {
                    val i = Intent(this, ChatActivity::class.java)
                    i.putExtra(K.MY_ID, getUid())
                    i.putExtra(K.OTHER_ID, depot.sellerId)
                    i.putExtra(K.CHAT_NAME, depot.sellerName)
                    this.startActivity(i)
                    AppUtils.animateFadein(this)
                }
            }
        }
    }

    private fun buyerOptions(depot: Depot) {
        val builder = AlertDialog.Builder(this)

        if (depot.watchlist.containsKey(getUid())) {
            val items = arrayOf<CharSequence>("Batalkan Berlangganan Pada Depot Ini?")

            builder.setItems(items) { _, item ->
                when(item) {
                    0 -> {
                        removeFromWatchlist(depot)
                    }
                }
            }

        } else {
            val items = arrayOf<CharSequence>("Berlangganan Pada Depot Ini?")

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
            toast("${depot.depotName} added to watchlist")

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
            toast("${depot.depotName}removed from watchlist")

            getFirestore().collection(K.WATCHLIST).document(getUid()).collection(K.DEPOTS).document(depot.id!!).delete()
        }.addOnFailureListener {
            Timber.e("Error removing ${depot.id} from watchlist: $it")
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
