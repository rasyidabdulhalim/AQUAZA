package com.rasyidabdulhalim.aquaza.fragments


import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.adapters.EmployeeAdapter
import com.rasyidabdulhalim.aquaza.callbacks.EmployeCallBack
import com.rasyidabdulhalim.aquaza.commoners.BaseFragment
import com.rasyidabdulhalim.aquaza.models.User
import com.rasyidabdulhalim.aquaza.utils.RecyclerFormatter
import kotlinx.android.synthetic.main.fragment_my_employee.*

class MyEmployeeBoardFragment : BaseFragment(), EmployeCallBack {
    private lateinit var employeeAdapter: EmployeeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_board_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onClick(v: View, employee: User) {
        TODO("Not yet implemented")
    }

}
