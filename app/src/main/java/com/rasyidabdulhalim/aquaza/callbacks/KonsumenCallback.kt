package com.rasyidabdulhalim.aquaza.callbacks

import android.view.View
import com.rasyidabdulhalim.aquaza.models.Konsumen

interface KonsumenCallback {
    fun onClick(v: View, konsumen: Konsumen)

}