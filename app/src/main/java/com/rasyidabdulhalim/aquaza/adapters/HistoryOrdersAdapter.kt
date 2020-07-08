package com.rasyidabdulhalim.aquaza.adapters

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.callbacks.OrderCallBack
import com.rasyidabdulhalim.aquaza.databinding.HistoryOrderItemBinding
import com.rasyidabdulhalim.aquaza.models.Order
import com.rasyidabdulhalim.aquaza.utils.TimeFormatter
import com.rasyidabdulhalim.aquaza.utils.inflate
import java.text.NumberFormat
import java.util.*

class HistoryOrdersAdapter(private val callback: OrderCallBack) : RecyclerView.Adapter<HistoryOrdersAdapter.OrderHolder>(){
    private val orders = mutableListOf<Order>()
    fun addItem(order: Order) {
        orders.add(order)

        notifyItemInserted(orders.size-1)
    }

    fun addItems(orders: MutableList<Order>) {
        val initialPos = orders.size
        orders.addAll(orders)
        notifyItemRangeInserted(initialPos, orders.size)
    }

    fun updateItem(updatedOrder: Order) {
        for ((index, car) in orders.withIndex()) {
            if (updatedOrder.id == car.id) {
                orders[index] = updatedOrder
                notifyItemChanged(index, updatedOrder)
            }
        }
    }
    fun clear() {
        val size: Int = orders.size
        orders.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun removeItem(removedOrder: Order) {
        var indexToRemove: Int = -1

        for ((index, order) in orders.withIndex()) {
            if (removedOrder.id == order.id) {
                indexToRemove = index
            }
        }

        orders.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHolder {
        return OrderHolder(parent.inflate(R.layout.history_order_item), callback)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: OrderHolder, position: Int) {
        holder.bind(orders[position])
    }

    class OrderHolder(private val binding: HistoryOrderItemBinding, callback: OrderCallBack) : RecyclerView.ViewHolder(binding.root) {
        private var localeID: Locale = Locale("in", "ID")
        private var formatRupiah : NumberFormat = NumberFormat.getCurrencyInstance(localeID)
        init {
            //  binding.location.setDrawable(setDrawable(context, Ionicons.Icon.ion_location, R.color.secondaryText, 12))
            binding.callback = callback
        }

        fun bind(order: Order) {
            binding.data = order
            binding.time = TimeFormatter().getTimeStamp(order.time!!)
            binding.qty=""+order.quantity+" Galon"
            binding.price = formatRupiah.format(order.price!!.toDouble())
            binding.isMine = (order.buyerId == FirebaseAuth.getInstance().currentUser?.uid)
        }

    }

}