package com.rasyidabdulhalim.aquaza.interfaces

import com.rasyidabdulhalim.aquaza.models.Driver


interface FirebaseDriverListener {

    fun onDriverAdded(driver: Driver)

    fun onDriverRemoved(driver: Driver)

    fun onDriverUpdated(driver: Driver)
}