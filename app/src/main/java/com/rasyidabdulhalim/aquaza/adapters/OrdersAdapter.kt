package com.rasyidabdulhalim.aquaza.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.callbacks.OrderCallBack
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.databinding.OrderItemBinding
import com.rasyidabdulhalim.aquaza.models.Order
import com.rasyidabdulhalim.aquaza.utils.TimeFormatter
import com.rasyidabdulhalim.aquaza.utils.inflate
import java.text.NumberFormat
import java.util.*
import java.util.logging.Filter

class OrdersAdapter(private val context: Context, private val callback: OrderCallBack) :
    RecyclerView.Adapter<OrdersAdapter.ItemHolder>() {
    private val orders = mutableListOf<Order>()

    fun addItem(item: Order) {
        orders.add(item)
        notifyItemInserted(orders.size - 1)
    }

    fun addItems(items: MutableList<Order>) {
        val initialPos = items.size
        items.addAll(items)
        notifyItemRangeInserted(initialPos, items.size)
    }

    fun updateItem(updateItem: Order) {
        for ((index, order) in orders.withIndex()) {
            if (updateItem.id == order.id) {
                orders[index] = updateItem
                notifyItemChanged(index, updateItem)
            }
        }
    }

    fun removeItem(removedItem: Order) {
        var indexToRemove: Int = -1

        for ((index, depot) in orders.withIndex()) {
            if (removedItem.id == depot.id) {
                indexToRemove = index
            }
        }
        orders.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    fun clear() {
        val size: Int = orders.size
        orders.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(parent.inflate(R.layout.order_item), callback)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(orders[position])
    }

    class ItemHolder(private val binding: OrderItemBinding, callback: OrderCallBack) :
        RecyclerView.ViewHolder(binding.root) {
        private var localeID: Locale = Locale("in", "ID")
        private var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)

        init {
            binding.callback = callback
        }

        fun bind(order: Order) {
            binding.data = order
            binding.time = TimeFormatter().getTimeStamp(order.time!!)
            binding.qty = "" + order.quantity + " Galon"
            binding.price = formatRupiah.format(order.price!!.toDouble())
            binding.isMine = (order.buyerId == FirebaseAuth.getInstance().currentUser?.uid)
            if (binding.isMine == true) {
                if (order.status == K.ONPROSES) {
                    binding.cancel.isEnabled = false
                    binding.cancel.isClickable = false
                    binding.cancel.text = "Please Wait"
                    binding.cancel.setBackgroundColor(R.drawable.button_tag_with_state)
                } else {
                    binding.cancel.text = "Batalkan"
                    binding.cancel.setBackgroundColor(R.drawable.rounded_orange_button)
                }
            } else {
                if (order.status == K.ONPROSES) {
                    binding.cancel.isEnabled = false
                    binding.cancel.isClickable = false
                    binding.cancel.text = "Taked"
                    binding.cancel.setBackgroundColor(R.drawable.button_round_cornet_grey)
                } else {
                    binding.cancel.text = "Take Order"
                    binding.cancel.setBackgroundColor(Color.parseColor("#727BFA"))
                }
            }
        }

    }

}