package com.rasyidabdulhalim.aquaza.models

data class Chat(
    var id: String? = null,
    var username: String? = null,
    var time: Long? = null,
    var message: String? = null,
    var senderId: String? = null
)