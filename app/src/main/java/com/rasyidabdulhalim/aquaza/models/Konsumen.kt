package com.rasyidabdulhalim.aquaza.models

import java.io.Serializable

data class Konsumen(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var address: String? = null,
    var defaultlat: Double? = null,
    var defaultlng: Double? = null,
    var status: String? = null,
    var depotId: String? = null,
    var depotName: String? = null,
    var dateCreated: String? = null,
    var dateModified: String? = null,
    var avatar: String? = null
) : Serializable