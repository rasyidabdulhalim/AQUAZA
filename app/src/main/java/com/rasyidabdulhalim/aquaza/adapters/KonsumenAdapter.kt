package com.rasyidabdulhalim.aquaza.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.callbacks.KonsumenCallback
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.databinding.KonsumenItemBinding
import com.rasyidabdulhalim.aquaza.models.Konsumen
import com.rasyidabdulhalim.aquaza.utils.inflate
import java.text.NumberFormat
import java.util.*

class KonsumenAdapter(private val context: Context, private val callback: KonsumenCallback) :
    RecyclerView.Adapter<KonsumenAdapter.ItemHolder>() {
    private val konsumens = mutableListOf<Konsumen>()

    fun addItem(item: Konsumen) {
        konsumens.add(item)
        notifyItemInserted(konsumens.size - 1)
    }

    fun addItems(items: MutableList<Konsumen>) {
        val initialPos = items.size
        items.addAll(items)
        notifyItemRangeInserted(initialPos, items.size)
    }

    fun updateItem(updateItem: Konsumen) {
        for ((index, order) in konsumens.withIndex()) {
            if (updateItem.id == order.id) {
                konsumens[index] = updateItem
                notifyItemChanged(index, updateItem)
            }
        }
    }

    fun removeItem(removedItem: Konsumen) {
        var indexToRemove: Int = -1

        for ((index, depot) in konsumens.withIndex()) {
            if (removedItem.id == depot.id) {
                indexToRemove = index
            }
        }
        konsumens.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    fun clear() {
        val size: Int = konsumens.size
        konsumens.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(parent.inflate(R.layout.konsumen_item), callback)
    }

    override fun getItemCount(): Int = konsumens.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(konsumens[position])
    }

    class ItemHolder(private val binding: KonsumenItemBinding, callback: KonsumenCallback) :
        RecyclerView.ViewHolder(binding.root) {
        private var localeID: Locale = Locale("in", "ID")
        private var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)

        init {
            binding.callback = callback
        }

        fun bind(konsumen: Konsumen) {
            binding.data = konsumen
            binding.isKonsumen = (konsumen.status == K.SUBSCIBE)
            if (konsumen.status == K.SUBSCIBE) {
                binding.cancel.visibility = View.GONE
                binding.action.visibility = View.GONE
                binding.contact.visibility = View.GONE
            }
        }

    }

}