package com.rasyidabdulhalim.aquaza.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val typeface: Typeface =
            Typeface.createFromAsset(assets, "carbon_bl.ttf")
            tv_app_name.typeface = typeface

        Handler().postDelayed(
            {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            },
            1200
        )
    }
}