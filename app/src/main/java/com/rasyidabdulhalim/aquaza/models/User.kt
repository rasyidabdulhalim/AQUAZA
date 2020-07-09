package com.rasyidabdulhalim.aquaza.models

import java.io.Serializable

data class User(
        var id: String? = null,
        var name: String? = null,
        var email: String? = null,
        var phone: String? = null,
        var address: String? = null,
        var defaultlat: String? = null,
        var defaultlng: String? = null,
        var status: String? = null,
        var mydepot: String? = null,
        var myshift: String? = null,
        var dateCreated: String? = null,
        var dateModified: String? = null,
        var avatar: String? = null
): Serializable {
}