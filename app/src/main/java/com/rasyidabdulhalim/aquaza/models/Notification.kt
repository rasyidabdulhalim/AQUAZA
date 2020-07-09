package com.rasyidabdulhalim.aquaza.models

data class Notification(
        var id: String? = null,
        var actionType: String? = null,
        var summary: String? = null,
        var avatar: String? = null,
        var uid: String? = null,
        var time: Long? = null
) {
}