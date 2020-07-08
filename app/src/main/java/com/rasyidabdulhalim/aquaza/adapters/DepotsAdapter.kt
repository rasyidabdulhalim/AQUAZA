package com.rasyidabdulhalim.aquaza.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.callbacks.DepotCallback
import com.rasyidabdulhalim.aquaza.models.Depot
import com.rasyidabdulhalim.aquaza.utils.TimeFormatter
import com.rasyidabdulhalim.aquaza.utils.inflate
import com.google.firebase.auth.FirebaseAuth
import com.rasyidabdulhalim.aquaza.databinding.DepotItemBinding

class DepotsAdapter(private val context: Context, private val callback: DepotCallback) : RecyclerView.Adapter<DepotsAdapter.DepotHolder>(){
    private val depots = mutableListOf<Depot>()

    fun addCar(depot: Depot) {
        depots.add(depot)
        notifyItemInserted(depots.size-1)
    }

    fun addCars(depots: MutableList<Depot>) {
        val initialPos = depots.size
        depots.addAll(depots)
        notifyItemRangeInserted(initialPos, depots.size)
    }

    fun updateCar(updatedDepot: Depot) {
        for ((index, depot) in depots.withIndex()) {
            if (updatedDepot.id == depot.id) {
                depots[index] = updatedDepot
                notifyItemChanged(index, updatedDepot)
            }
        }
    }

    fun removeCar(removedDepot: Depot) {
        var indexToRemove: Int = -1

        for ((index, depot) in depots.withIndex()) {
            if (removedDepot.id == depot.id) {
                indexToRemove = index
            }
        }
        depots.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepotHolder {
        return DepotHolder(parent.inflate(R.layout.depot_item), callback)
    }

    override fun getItemCount(): Int = depots.size

    override fun onBindViewHolder(holder: DepotHolder, position: Int) {
        holder.bind(depots[position])
    }

    class DepotHolder(private val binding:DepotItemBinding, callback: DepotCallback) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.callback = callback
        }

        fun bind(depot: Depot) {
            binding.data = depot
            binding.time = TimeFormatter().getTimeStamp(depot.time!!)
            binding.isMine = (depot.sellerId == FirebaseAuth.getInstance().currentUser?.uid)
        }

    }

}