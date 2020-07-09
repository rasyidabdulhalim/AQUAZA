package com.rasyidabdulhalim.aquaza.callbacks

import android.view.View
import com.rasyidabdulhalim.aquaza.models.Konsumen
import com.rasyidabdulhalim.aquaza.models.User

interface KonsumenCallback {
    fun onClick(v: View, konsumen: Konsumen)

}