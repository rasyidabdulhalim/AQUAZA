package com.rasyidabdulhalim.aquaza.callbacks

import android.view.View
import com.rasyidabdulhalim.aquaza.models.Depot

interface DepotCallback {

    fun onClick(v: View, depot: Depot)

}