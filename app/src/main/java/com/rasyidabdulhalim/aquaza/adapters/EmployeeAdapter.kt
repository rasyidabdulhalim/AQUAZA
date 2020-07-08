package com.rasyidabdulhalim.aquaza.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.callbacks.EmployeCallBack
import com.rasyidabdulhalim.aquaza.databinding.EmployeeItemBinding
import com.rasyidabdulhalim.aquaza.models.User
import com.rasyidabdulhalim.aquaza.utils.inflate

class EmployeeAdapter(private val context: Context, private val callback: EmployeCallBack) : RecyclerView.Adapter<EmployeeAdapter.EmployeeHolder>(){
    private val users = mutableListOf<User>()

    fun addEmployee(employee: User) {
        users.add(employee)
        notifyItemInserted(users.size-1)
    }

    fun addEmployees(employees: MutableList<User>) {
        val initialPos = employees.size
        employees.addAll(employees)
        notifyItemRangeInserted(initialPos, employees.size)
    }

    fun updateEmployee(updatedEmployee: User) {
        for ((index, user) in users.withIndex()) {
            if (updatedEmployee.id == user.id) {
                users[index] = updatedEmployee
                notifyItemChanged(index, updatedEmployee)
            }
        }
    }

    fun removeEmployee(removedEmployee: User) {
        var indexToRemove: Int = -1

        for ((index, user) in users.withIndex()) {
            if (removedEmployee.id == user.id) {
                indexToRemove = index
            }
        }
        users.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeHolder {
        return EmployeeHolder(parent.inflate(R.layout.employee_item), callback)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: EmployeeHolder, position: Int) {
        holder.bind(users[position])
    }

    class EmployeeHolder(private val binding:EmployeeItemBinding, callback: EmployeCallBack) : RecyclerView.ViewHolder(binding.root) {

        init {
          //  binding.location.setDrawable(setDrawable(context, Ionicons.Icon.ion_location, R.color.secondaryText, 12))
            binding.callback = callback
        }

        fun bind(employee: User) {
            binding.data = employee
        }

    }

}