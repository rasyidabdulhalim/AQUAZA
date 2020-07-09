package com.rasyidabdulhalim.aquaza.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.alert

class SettingsActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Settings"

        notifications()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.about -> about()
            R.id.terms -> tou()
            R.id.privacy -> privacy()
            R.id.rate -> rate()
            R.id.feedback -> feedback()
            R.id.exit -> logOut()
        }
    }

    private fun notifications() {
        notifications.setOnCheckedChangeListener { _, isChecked ->
            // prefs[K.NOTIFICATIONS] = isChecked
        }

        news.setOnCheckedChangeListener { _, isChecked ->
            // prefs[K.NEWS] = isChecked
        }
    }

    private fun about() {

    }

    private fun tou() {

    }

    private fun privacy() {

    }

    @SuppressLint("InlinedApi")
    private fun rate() {
        val uri = Uri.parse(resources.getString(R.string.play_store_link) + this.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(resources.getString(R.string.play_store_link) + this.packageName)
                )
            )
        }
    }

    private fun feedback() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto: devtirgei@gmail.com")
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Automart")
        startActivity(Intent.createChooser(emailIntent, "Send feedback"))
    }

    private fun logOut() {
        Handler().postDelayed({
            alert("Are you sure you want to log out?") {
                title = "Log out"
                positiveButton("LOG OUT") {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    firebaseAuth.signOut()
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(K.TOPIC_GLOBAL)
                    PreferenceManager.getDefaultSharedPreferences(this as Context).edit().clear()
                        .apply()//clear first
                    startActivity(Intent(this@SettingsActivity, AuthActivity::class.java))
                    AppUtils.animateEnterLeft(this@SettingsActivity)
                    finish()
                }
                negativeButton("CANCEL") {}
            }.show()
        }, 300)
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
