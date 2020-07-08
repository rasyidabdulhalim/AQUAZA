package com.rasyidabdulhalim.aquaza.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.activities.AddEmployeActivity
import com.rasyidabdulhalim.aquaza.activities.DepotActivity
import com.rasyidabdulhalim.aquaza.adapters.EmployeeAdapter
import com.rasyidabdulhalim.aquaza.callbacks.EmployeCallBack
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseFragment
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.User
import com.rasyidabdulhalim.aquaza.utils.RecyclerFormatter
import com.rasyidabdulhalim.aquaza.utils.hideView
import com.rasyidabdulhalim.aquaza.utils.showView
import kotlinx.android.synthetic.main.fragment_my_employee.empty
import kotlinx.android.synthetic.main.fragment_my_employee.rv
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber

class MyKonsumenRequestFragment : BaseFragment(), EmployeCallBack {
    private lateinit var employeeAdapter: EmployeeAdapter

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

        employeeAdapter = EmployeeAdapter(activity!!, this)
        rv.adapter = employeeAdapter
        rv.showShimmerAdapter()
    }

    private fun loadRequestKonsumen() {
        getFirestore().collection(K.WATCHLIST).document().collection(K.DEPOTS)
            .orderBy(K.TIMESTAMP, Query.Direction.DESCENDING)
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
                                val car = docChange.document.toObject(User::class.java)
                                employeeAdapter.addEmployee(car)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val car = docChange.document.toObject(User::class.java)
                                employeeAdapter.updateEmployee(car)
                            }

                            DocumentChange.Type.REMOVED -> {
                                val car = docChange.document.toObject(User::class.java)
                                employeeAdapter.removeEmployee(car)
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
    override fun onClick(v: View, employee: User) {
        when(v.id){
            R.id.editEmp->{
                val i = Intent(activity, AddEmployeActivity::class.java)
                i.putExtra(K.USERS, employee)
                startActivity(i)
                AppUtils.animateFadein(activity!!)
            }
            R.id.PecatEmp->{
                activity?.alert("Pecat Karyawan?") {
                    positiveButton("YES") {
                        getFirestore().collection(K.USERS).document(employee.id!!).delete()
                            .addOnSuccessListener {
                                activity?.toast("Karyawan Telah Dipecat")
                                employeeAdapter.removeEmployee(employee)
                            }
                            .addOnFailureListener {
                                Timber.e("Error deleting ${employee.name}")
                                activity?.toast("Error deleting ${employee.name}")
                            }
                    }
                    negativeButton("CANCEL") {}
                }!!.show()
            }
            R.id.image -> {
                val i = Intent(activity, DepotActivity::class.java)
                i.putExtra(K.USERS, employee)
                startActivity(i)
                AppUtils.animateFadein(activity!!)
            }
        }
    }
}