package com.rasyidabdulhalim.aquaza.activities

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.adapters.ImagesAdapter
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Notification
import com.rasyidabdulhalim.aquaza.models.User
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper.get
import kotlinx.android.synthetic.main.activity_add_notifikasi.*
import org.jetbrains.anko.toast
import timber.log.Timber

class AddNotificationActivity : BaseActivity() {
    private var pickedImages = mutableListOf<Uri>()
    private lateinit var imagesAdapter: ImagesAdapter
    private val images = mutableMapOf<String, String>()
    private var notice = Notification()
    private var employe = User()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notifikasi)
        prefs = PreferenceHelper.defaultPrefs(this)
        if (intent.getSerializableExtra(K.USER) != null) {
            employe = intent.getSerializableExtra(K.USER) as User
        }
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Send Notification"
        if (employe.id != null) {
            typeSendMessage.text = Editable.Factory.getInstance().newEditable(employe.id)
        } else {
            typeSendMessage.text = Editable.Factory.getInstance().newEditable("All")
        }
        buttonNotification.setOnClickListener {
addMessage()
        }

    }

    fun addMessage() {
        if (typeSendMessage.text.toString()==""){
            toast("Input Type Send Message")
            return
        }
        if (message.text.toString()==""){
            toast("Input Type Send Message")
            return
        }
        if (statusMessage.text.toString()==""){
            toast("Input Type Send Message")
            return
        }
        notice.id = getFirestore().collection(K.USERS).document().id
        notice.time = System.currentTimeMillis()
        notice.avatar = prefs[K.AVATAR, ""]
        notice.summary = message.text.toString()
        notice.uid = typeSendMessage.text.toString()
        notice.actionType = statusMessage.text.toString()
        getFirestore().collection(K.NOTIFICATIONS).document(notice.id!!).set(notice)
            .addOnSuccessListener {
                Timber.e("notice successfully Added")
                hideLoading()
                toast("Notice successfully Added")
                onBackPressed()
            }
            .addOnFailureListener {
                Timber.e("Error uploading: $it")
                hideLoading()

                toast("Error Add Notice. Please try again")

            }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.animateEnterLeft(this)

    }

}
