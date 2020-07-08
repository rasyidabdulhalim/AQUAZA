package com.rasyidabdulhalim.aquaza.callbacks

import android.view.View
import com.rasyidabdulhalim.aquaza.models.Order

interface OrderCallBack {

    fun onClick(v: View, order: Order)
}