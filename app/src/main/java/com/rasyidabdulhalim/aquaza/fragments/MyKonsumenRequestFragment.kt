package com.rasyidabdulhalim.aquaza.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentChange
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.activities.ChatActivity
import com.rasyidabdulhalim.aquaza.adapters.KonsumenAdapter
import com.rasyidabdulhalim.aquaza.callbacks.KonsumenCallback
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseFragment
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Konsumen
import com.rasyidabdulhalim.aquaza.utils.RecyclerFormatter
import com.rasyidabdulhalim.aquaza.utils.hideView
import com.rasyidabdulhalim.aquaza.utils.showView
import kotlinx.android.synthetic.main.fragment_my_requestkonsumen.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber

class MyKonsumenRequestFragment : BaseFragment(), KonsumenCallback {
    private lateinit var konsumenAdapter: KonsumenAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_requestkonsumen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadRequestKonsumen()
    }

    private fun initViews(v: View) {
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(activity!!)
        rv.itemAnimator = DefaultItemAnimator()
        rv.addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        konsumenAdapter = KonsumenAdapter(activity!!, this)
        rv.adapter = konsumenAdapter
        rv.showShimmerAdapter()
    }

    private fun loadRequestKonsumen() {
        getFirestore().collection(K.WATCHLIST)
            .whereEqualTo("status",K.REQUEST)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching Employee $firebaseFirestoreException")
                    noEmployees()
                }

                if (querySnapshot == null || querySnapshot.isEmpty) {
                    noEmployees()
                } else {
                    hasEmployees()

                    for (docChange in querySnapshot.documentChanges) {

                        when(docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val konsumen = docChange.document.toObject(Konsumen::class.java)
                                konsumenAdapter.addItem(konsumen)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val konsumen = docChange.document.toObject(Konsumen::class.java)
                                konsumenAdapter.updateItem(konsumen)
                            }

                            DocumentChange.Type.REMOVED -> {
                                val konsumen = docChange.document.toObject(Konsumen::class.java)
                                konsumenAdapter.removeItem(konsumen)
                            }

                        }

                    }

                }
            }
    }

    private fun hasEmployees() {
        rv?.hideShimmerAdapter()
        empty?.hideView()
        rv?.showView()
    }

    private fun noEmployees() {
        rv?.hideShimmerAdapter()
        rv?.hideView()
        empty?.showView()
    }

    override fun onClick(v: View, konsumen: Konsumen) {
        when(v.id){
            R.id.cancel->{
                activity?.alert("Hapus Permintaan Berlangganan Dari User Ini?") {
                    positiveButton("YES") {
                        if(konsumenAdapter.itemCount==1){
                            konsumenAdapter.clear()
                            noEmployees()
                        }
                        getFirestore().collection(K.WATCHLIST).document(konsumen.id!!).delete()
                            .addOnSuccessListener {
                                activity?.toast("Permintaan Berhasil Dibatalkan")
                            }
                            .addOnFailureListener {
                                Timber.e("Error deleting ${konsumen.id}")
                                activity?.toast("Error deleting ${konsumen.id}")
                            }
                    }
                    negativeButton("CANCEL") {}
                }!!.show()
            }
            R.id.action->{
                activity?.alert("Konfirmasi Permintaan Berlangganan Dari User Ini?") {
                    positiveButton("YES") {
                        if(konsumenAdapter.itemCount==1){
                            konsumenAdapter.clear()
                            noEmployees()
                        }
                        konsumen.status=K.SUBSCIBE
                        getFirestore().collection(K.WATCHLIST).document(konsumen.id!!).set(konsumen).addOnSuccessListener {
                                activity?.toast("Konsumen Baru Berhasil Ditambahkan")
                            }
                            .addOnFailureListener {
                                Timber.e("Error deleting ${konsumen.id}")
                                activity?.toast("Error deleting ${konsumen.id}")
                            }
                    }
                    negativeButton("CANCEL") {}
                }!!.show()
            }
            R.id.contact -> {
                val i = Intent(activity, ChatActivity::class.java)
                i.putExtra(K.MY_ID, getUid())
                i.putExtra(K.OTHER_ID, konsumen.id)
                i.putExtra(K.CHAT_NAME, konsumen.name)
                activity!!.startActivity(i)
                AppUtils.animateFadein(activity!!)
            }
        }

    }
}
