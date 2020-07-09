package com.rasyidabdulhalim.aquaza.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.fragments.AuthLoginFragment
import com.rasyidabdulhalim.aquaza.fragments.AuthRegisterFragment
import com.rasyidabdulhalim.aquaza.utils.addFragment
import org.jetbrains.anko.toast

class AuthActivity : BaseActivity() {
    private lateinit var authLoginFragment: AuthLoginFragment
    private lateinit var authRegisterFragment: AuthRegisterFragment
    private var doubleBackToExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        checkIfLoggedIn()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_activity)

        authLoginFragment = AuthLoginFragment()
        authRegisterFragment = AuthRegisterFragment()

        addFragment(authLoginFragment, R.id.authHolder)
    }

    private fun checkIfLoggedIn() {
        if (getUser() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun onBackPressed() {
        if (!authRegisterFragment.backPressOkay() || !authLoginFragment.backPressOkay()) {
            toast("Please wait...")

        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            if (doubleBackToExit) {
                super.onBackPressed()
            } else {
                toast("Tap back again to exit")
                doubleBackToExit = true

                Handler().postDelayed({ doubleBackToExit = false }, 1500)
            }
        }

    }

}
