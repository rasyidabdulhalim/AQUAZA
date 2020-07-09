package com.rasyidabdulhalim.aquaza.models

import java.io.Serializable

data class Order(
    var id: String? = null,
    var buyerId: String? = null,
    var buyerName: String? = null,
    var depotName: String? = null,
    var depotId: String? = null,
    var driverId: String? = null,
    var driverName: String? = null,
    var time: Long? = null,
    var quantity: Int? = null,
    var price: String? = null,
    var location: String? = null,
    var lat: Double? = null,
    var lng: Double? = null,
    var description: String? = null,
    var status: String? = null
) : Serializable