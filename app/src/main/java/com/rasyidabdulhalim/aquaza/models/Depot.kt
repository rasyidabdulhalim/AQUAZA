package com.rasyidabdulhalim.aquaza.models

import java.io.Serializable

data class Depot(
        var id: String? = null,
        var depotName: String? = null,
        var sellerName: String? = null,
        var sellerId: String? = null,
        var time: Long? = null,
        var location: String? = null,
        var email: String? = null,
        var phone: String? = null,
        var description: String? = null,
        var price: String? = null,
        var image: String? = null,
        var images: MutableMap<String, String> = mutableMapOf(),
        var watchlist: MutableMap<String, Boolean> = mutableMapOf(),
        var status: String? = null
) : Serializable {
}