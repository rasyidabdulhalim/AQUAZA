package com.rasyidabdulhalim.aquaza.helpers

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.rasyidabdulhalim.aquaza.interfaces.FirebaseDriverListener
import com.rasyidabdulhalim.aquaza.models.Driver

class FirebaseEventListenerHelper(private val firebaseDriverListener: FirebaseDriverListener) :
    ChildEventListener {

    override fun onCancelled(p0: DatabaseError) {

    }

    override fun onChildMoved(p0: DataSnapshot, p1: String?) {

    }

    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        val driver = p0.getValue(Driver::class.java)
        firebaseDriverListener.onDriverUpdated(driver!!)
    }

    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
        val driver = p0.getValue(Driver::class.java)
        firebaseDriverListener.onDriverAdded(driver!!)
    }

    override fun onChildRemoved(p0: DataSnapshot) {
        val driver = p0.getValue(Driver::class.java)
        firebaseDriverListener.onDriverRemoved(driver!!)
    }
}