package com.rasyidabdulhalim.aquaza.models

import java.io.Serializable

data class Driver(
    val driverId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
) : Serializable